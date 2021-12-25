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
package org.hisp.dhis.tracker.validation.spike.one.hooks;

import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1118;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1120;

import java.util.Optional;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.tracker.TrackerType;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.report.TrackerErrorReport;
import org.hisp.dhis.tracker.report.TrackerWarningReport;
import org.hisp.dhis.tracker.validation.spike.one.ValidationHook;
import org.hisp.dhis.user.User;
import org.springframework.stereotype.Component;

@Component
public class AssignedUserValidationHook implements ValidationHook<Event>
{
    // TODO so now we have one hook that potentially adds 2 errors to a report.
    // How to organize, register this?
    @Override
    public Optional<TrackerErrorReport> validate( TrackerBundle bundle, Event event )
    {
        if ( event.getAssignedUser() == null )
        {
            return Optional.empty();
        }

        if ( isNotValidAssignedUserUid( event ) || assignedUserNotPresentInPreheat( bundle, event ) )
        {
            TrackerErrorReport error = TrackerErrorReport.builder()
                .uid( event.getUid() )
                .trackerType( TrackerType.EVENT )
                .errorCode( E1118 )
                .addArg( event.getAssignedUser() )
                .build( bundle );
            return Optional.of( error );
        }
        return Optional.empty();
    }

    // TODO double challenge. warning and second hook in a hook
    public Optional<TrackerWarningReport> validate2( TrackerBundle bundle, Event event )
    {
        if ( event.getAssignedUser() == null )
        {
            return Optional.empty();
        }

        if ( isNotEnabledUserAssignment( bundle, event ) )
        {
            TrackerWarningReport warning = TrackerWarningReport.builder()
                .uid( event.getUid() )
                .trackerType( TrackerType.EVENT )
                .warningCode( E1120 )
                .addArg( event.getProgramStage() )
                .build( bundle );
            return Optional.of( warning );
        }
        return Optional.empty();
    }

    private Boolean isNotEnabledUserAssignment( TrackerBundle bundle, Event event )
    {
        ProgramStage programStage = bundle.getPreheat().get( ProgramStage.class, event.getProgramStage() );
        Boolean userAssignmentEnabled = programStage.isEnableUserAssignment();

        return !Optional.ofNullable( userAssignmentEnabled )
            .orElse( false );
    }

    private boolean assignedUserNotPresentInPreheat( TrackerBundle bundle, Event event )
    {
        return bundle.getPreheat().get( User.class,
            event.getAssignedUser() ) == null;
    }

    private boolean isNotValidAssignedUserUid( Event event )
    {
        return !CodeGenerator.isValidUid( event.getAssignedUser() );
    }

}
