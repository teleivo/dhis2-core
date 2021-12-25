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
package org.hisp.dhis.tracker.validation.spike.base;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;

import org.hisp.dhis.tracker.ValidationMode;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.report.TrackerReportItem;
import org.hisp.dhis.tracker.report.TrackerValidationReport;

@AllArgsConstructor
public class SpikeValidationService implements ValidationService
{

    // TODO how can I have one collection of hooks setup in a declarative way in
    // the ServiceConfig
    // and then only dispatch the wanted type to the hook? again with
    // instanceof?
    // or would I best split the hooks per type?
    private final List<ValidationHook> hooks;

    public TrackerValidationReport validate( TrackerBundle bundle )
    {
        // TODO how to implement remove on error? and then remove invalid from
        // the bundle
        // make this part of the error?
        // TODO fix my generics in here
        // TODO implement a more evolved hook to see how this design affects
        // code arrangement
        // TODO implement this method for all entities. How to write it in a
        // generic way so that I automatically
        // dispatch only to hooks interested in the hook
        TrackerValidationReport report = new TrackerValidationReport();

        for ( Event event : bundle.getEvents() )
        {
            validate( report, bundle, event );
            if ( report.hasErrors() && bundle.getValidationMode() == ValidationMode.FAIL_FAST )
            {
                break;
            }
        }

        removeInvalid( report, bundle );
        return report;
    }

    private void validate( TrackerValidationReport report, TrackerBundle bundle, Event event )
    {
        for ( ValidationHook hook : hooks )
        {
            Optional<? extends TrackerReportItem> item = hook.validate( bundle, event );
            if ( item.isPresent() )
            {
                report.add( item.get() );
                // TODO only exit if hook specifies remove on error
                return;
            }
        }
    }

    private void removeInvalid( TrackerValidationReport report, TrackerBundle bundle )
    {
        bundle.setEvents( bundle.getEvents().stream().filter(
            e -> !report.isInvalid( e ) )
            .collect( Collectors.toList() ) );
        bundle.setEnrollments( bundle.getEnrollments().stream().filter(
            e -> !report.isInvalid( e ) )
            .collect( Collectors.toList() ) );
        bundle.setTrackedEntities( bundle.getTrackedEntities().stream().filter(
            e -> !report.isInvalid( e ) )
            .collect( Collectors.toList() ) );
        bundle.setRelationships( bundle.getRelationships().stream().filter(
            e -> !report.isInvalid( e ) )
            .collect( Collectors.toList() ) );
    }
}
