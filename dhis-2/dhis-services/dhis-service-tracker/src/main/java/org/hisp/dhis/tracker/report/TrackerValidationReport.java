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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hisp.dhis.tracker.TrackerType;
import org.hisp.dhis.tracker.domain.TrackerDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackerValidationReport
{
    @JsonProperty
    @Builder.Default
    private List<TrackerErrorReport> errorReports = new ArrayList<>();

    @JsonProperty
    @Builder.Default
    private List<TrackerWarningReport> warningReports = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    private List<TrackerValidationHookTimerReport> performanceReport = new ArrayList<>();

    /*
     * A map that keep tracks of all the invalid Tracker objects encountered
     * during the validation process
     */
    private Map<TrackerType, List<String>> invalidDTOs = new HashMap<>();

    // -----------------------------------------------------------------------------------
    // Utility Methods
    // -----------------------------------------------------------------------------------
    public void add( TrackerReportItem item )
    {
        // TODO is there an alternative to achieving this?
        // is this frowned upon ;)
        if ( item instanceof TrackerErrorReport )
        {
            this.add( (TrackerErrorReport) item );
        }
        else if ( item instanceof TrackerWarningReport )
        {
            this.add( (TrackerWarningReport) item );
        }
        else
        {
            throw new UnsupportedOperationException( "only TrackerErrorReport and TrackerWarningReport are supported" );
        }
    }

    public void add( TrackerErrorReport error )
    {
        this.invalidDTOs.computeIfAbsent( error.getTrackerType(), k -> new ArrayList<>() ).add( error.getUid() );
        this.errorReports.add( error );
    }

    public void add( TrackerWarningReport warning )
    {
        this.warningReports.add( warning );
    }

    public void add( TrackerValidationReport validationReport )
    {
        add( validationReport.getErrorReports() );
        addWarnings( validationReport.getWarningReports() );
        addPerfReports( validationReport.getPerformanceReport() );
    }

    public void add( List<TrackerErrorReport> errorReports )
    {
        for ( TrackerErrorReport errorReport : errorReports )
        {
            addErrorIfNotExisting( errorReport );
        }
    }

    public void addWarnings( List<TrackerWarningReport> warningReportsReports )
    {
        for ( TrackerWarningReport warningReport : warningReportsReports )
        {
            addWarningIfNotExisting( warningReport );
        }
    }

    public void addPerfReports( List<TrackerValidationHookTimerReport> reports )
    {
        this.performanceReport.addAll( reports );
    }

    public void add( TrackerValidationHookTimerReport report )
    {
        performanceReport.add( report );
    }

    public boolean hasErrors()
    {
        return !errorReports.isEmpty();
    }

    /**
     * Returns the size of all the Tracker DTO that did not pass validation
     */
    public long size()
    {

        return this.getErrorReports().stream().map( TrackerErrorReport::getUid ).distinct().count();
    }

    private void addErrorIfNotExisting( TrackerErrorReport report )
    {
        if ( !this.errorReports.contains( report ) )
        {
            this.errorReports.add( report );
        }
    }

    private void addWarningIfNotExisting( TrackerWarningReport report )
    {
        if ( !this.warningReports.contains( report ) )
        {
            this.warningReports.add( report );
        }
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

    public boolean hasErrorReport( Predicate<TrackerErrorReport> test )
    {
        return errorReports.stream().anyMatch( test );
    }

    public boolean hasWarningReport( Predicate<TrackerWarningReport> test )
    {
        return warningReports.stream().anyMatch( test );
    }
}
