/*
 * Copyright (c) 2004-2021, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.tracker.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import lombok.Data;

import org.hisp.dhis.tracker.TrackerType;
import org.hisp.dhis.tracker.ValidationMode;
import org.hisp.dhis.tracker.domain.TrackerDto;
import org.hisp.dhis.tracker.validation.TrackerImportValidationContext;
import org.hisp.dhis.tracker.validation.ValidationFailFastException;

/**
 * A class that collects {@link TrackerErrorReport} during the validation
 * process.
 *
 * Each {@link TrackerErrorReport} collection is connected to a specific Tracker
 * entity (Tracked Entity, Enrollment, etc.) via the "mainUid" attribute
 *
 *
 * @author Morten Svanæs <msvanaes@dhis2.org>
 */
@Data
// TODO: should this be "ValidationReporter" since it does not only report
// errors ?
public class ValidationErrorReporter
{
    private final TrackerValidationReport report;

    private final boolean isFailFast;

    private final TrackerImportValidationContext validationContext;

    /*
     * A map that keep tracks of all the invalid Tracker objects encountered
     * during the validation process
     */
    private Map<TrackerType, List<String>> invalidDTOs;

    public static ValidationErrorReporter emptyReporter()
    {
        return new ValidationErrorReporter();
    }

    private ValidationErrorReporter()
    {
        this.report = new TrackerValidationReport();
        this.isFailFast = false;
        this.validationContext = null;
        this.invalidDTOs = new HashMap<>();
    }

    public ValidationErrorReporter( TrackerImportValidationContext context )
    {
        this.report = new TrackerValidationReport();
        this.validationContext = context;
        this.isFailFast = validationContext.getBundle().getValidationMode() == ValidationMode.FAIL_FAST;
        this.invalidDTOs = new HashMap<>();
    }

    public boolean hasErrors()
    {
        return this.report.hasErrors();
    }

    public boolean hasErrorReport( Predicate<TrackerErrorReport> test )
    {
        return this.report.hasErrorReport( test );
    }

    public boolean hasWarningReport( Predicate<TrackerWarningReport> test )
    {
        return this.report.hasWarningReport( test );
    }

    public boolean hasWarnings()
    {
        return this.report.hasWarnings();
    }

    public void addError( TrackerErrorReport error )
    {
        this.report.add( error );
        this.invalidDTOs.computeIfAbsent( error.getTrackerType(), k -> new ArrayList<>() ).add( error.getUid() );

        if ( isFailFast() )
        {
            // TODO(TECH-880) we do not need to pass the error report via the
            // exception
            // there is only one error report anymore which contains all errors.
            throw new ValidationFailFastException( this.getReport().getErrorReports() );
        }
    }

    public void addWarning( TrackerWarningReport warning )
    {
        this.report.addWarning( warning );
    }

    // TODO(TECH-880) investigate if we can replace this
    // client code is interested in size(), isEmpty(), and finding a specific
    // error
    // this can, is already at least partially provided by
    // TrackerValidationReport
    public List<TrackerErrorReport> getReportList()
    {
        return Collections.unmodifiableList( this.report.getErrorReports() );
    }

    /**
     * Checks if the provided uid and Tracker Type is part of the invalid
     * entities
     */
    public boolean isInvalid( TrackerType trackerType, String uid )
    {
        return this.invalidDTOs.getOrDefault( trackerType, new ArrayList<>() ).contains( uid );
    }

    public boolean isInvalid( TrackerDto dto )
    {
        return this.isInvalid( dto.getTrackerType(), dto.getUid() );
    }
}