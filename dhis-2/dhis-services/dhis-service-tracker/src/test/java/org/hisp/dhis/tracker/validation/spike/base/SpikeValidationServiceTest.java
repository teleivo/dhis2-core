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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.tracker.TrackerType;
import org.hisp.dhis.tracker.ValidationMode;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.hisp.dhis.tracker.report.TrackerErrorReport;
import org.hisp.dhis.tracker.report.TrackerValidationReport;
import org.hisp.dhis.tracker.validation.spike.base.hooks.AssignedUserValidationHook;
import org.hisp.dhis.tracker.validation.spike.base.hooks.UidValidationHook;
import org.hisp.dhis.user.User;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class SpikeValidationServiceTest
{

    @Test
    void reportAndRemoveInvalidFromBundleWithRemoveOnErrorHook()
    {
        Event validEvent = event();
        Event eventWithInvalidUid = event();
        eventWithInvalidUid.setEvent( "invalidUid" );
        // invalid assigned user because the user is not in the preheat
        Event eventWithInvalidAssignedUser = event();
        eventWithInvalidAssignedUser.setAssignedUser( CodeGenerator.generateUid() );
        List<Event> events = new ArrayList<>();
        events.add( eventWithInvalidUid );
        events.add( validEvent );
        events.add( eventWithInvalidAssignedUser );

        TrackerPreheat preheat = mock( TrackerPreheat.class );
        when( preheat.get( User.class, validEvent.getAssignedUser() ) ).thenReturn( new User() );
        when( preheat.get( User.class, eventWithInvalidUid.getAssignedUser() ) ).thenReturn( new User() );
        when( preheat.get( User.class, eventWithInvalidAssignedUser.getAssignedUser() ) ).thenReturn( null );
        TrackerBundle bundle = TrackerBundle.builder()
            .validationMode( ValidationMode.FULL )
            .skipRuleEngine( true )
            .preheat( preheat )
            .events( events )
            .build();

        ValidationService validationService = new SpikeValidationService(
            List.of( new UidValidationHook(), new AssignedUserValidationHook() ) );

        TrackerValidationReport report = validationService.validate( bundle );

        assertTrue( report.hasErrors() );
        assertEquals( 2, report.getErrorReports().size() );
        assertTrue( report.hasErrorReport( err -> TrackerErrorCode.E1048 == err.getErrorCode()
            && TrackerType.EVENT == err.getTrackerType()
            && eventWithInvalidUid.getUid().equals( err.getUid() ) ) );
        assertTrue( report.hasErrorReport( err -> TrackerErrorCode.E1118 == err.getErrorCode()
            && TrackerType.EVENT == err.getTrackerType()
            && eventWithInvalidAssignedUser.getUid().equals( err.getUid() ) ) );

        assertFalse( bundle.getEvents().contains( eventWithInvalidUid ) );
        assertFalse( bundle.getEvents().contains( eventWithInvalidAssignedUser ) );
        assertTrue( bundle.getEvents().contains( validEvent ) );
    }

    @NotNull
    private Event event()
    {
        Event event = new Event();
        event.setEvent( CodeGenerator.generateUid() );
        event.setAssignedUser( CodeGenerator.generateUid() );
        return event;
    }

    @NotNull
    private Enrollment enrollment()
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollment( CodeGenerator.generateUid() );
        return enrollment;
    }

    @Test
    void failFastDoesNotCallHooksAfterFailure()
    {
        Event validEvent = event();
        Event eventWithInvalidUid = event();
        eventWithInvalidUid.setEvent( "invalidUid" );
        // invalid assigned user because the user is not in the preheat
        Event eventWithInvalidAssignedUser = event();
        eventWithInvalidAssignedUser.setAssignedUser( CodeGenerator.generateUid() );
        List<Event> events = new ArrayList<>();
        events.add( eventWithInvalidUid );
        events.add( validEvent );
        events.add( eventWithInvalidAssignedUser );

        TrackerPreheat preheat = mock( TrackerPreheat.class );
        when( preheat.get( User.class, validEvent.getAssignedUser() ) ).thenReturn( new User() );
        when( preheat.get( User.class, eventWithInvalidUid.getAssignedUser() ) ).thenReturn( new User() );
        when( preheat.get( User.class, eventWithInvalidAssignedUser.getAssignedUser() ) ).thenReturn( null );
        TrackerBundle bundle = TrackerBundle.builder()
            .validationMode( ValidationMode.FAIL_FAST )
            .skipRuleEngine( true )
            .preheat( preheat )
            .events( events )
            .build();

        ValidationHook hook2 = mock( ValidationHook.class );
        ValidationService validationService = new SpikeValidationService( List.of(
            new UidValidationHook(),
            hook2,
            ( b, e ) -> Optional.of( TrackerErrorReport.builder()
                .errorCode( TrackerErrorCode.E1048 )
                .uid( e.getUid() )
                .trackerType( e.getTrackerType() )
                .build( b ) ) ) );

        TrackerValidationReport report = validationService.validate( bundle );

        assertTrue( report.hasErrors() );
        assertEquals( 1, report.getErrorReports().size() );
        assertTrue( report.hasErrorReport( err -> TrackerErrorCode.E1048 == err.getErrorCode()
            && TrackerType.EVENT == err.getTrackerType()
            && eventWithInvalidUid.getUid().equals( err.getUid() ) ) );

        assertFalse( bundle.getEvents().contains( eventWithInvalidUid ) );
        assertTrue( bundle.getEvents().contains( eventWithInvalidAssignedUser ) );
        assertTrue( bundle.getEvents().contains( validEvent ) );

        verifyNoInteractions( hook2 );
    }

    interface ReportItem
    {
        String getMessage();

        TrackerErrorCode getCode();

        TrackerType getType();

        String getUid();
    }

    static class Error implements ReportItem
    {

        @Override
        public String getMessage()
        {
            return "error";
        }

        @Override
        public TrackerErrorCode getCode()
        {
            return null;
        }

        @Override
        public TrackerType getType()
        {
            return null;
        }

        @Override
        public String getUid()
        {
            return null;
        }
    }

    static class Warning implements ReportItem
    {

        @Override
        public String getMessage()
        {
            return "warning";
        }

        @Override
        public TrackerErrorCode getCode()
        {
            return null;
        }

        @Override
        public TrackerType getType()
        {
            return null;
        }

        @Override
        public String getUid()
        {
            return null;
        }
    }

    class Report
    {
        List<Error> errors;

        List<Warning> warnings;

        void add( Error error )
        {
            System.out.println( "error" );
        }

        void add( Warning warning )
        {
            System.out.println( "warning" );
        }

        void add( ReportItem item )
        {
            // TODO is there a way to split them into different collections
            // based on its concrete type?
            // other than using instance of or reflection?
            System.out.println( "item" );
            System.out.printf( "is error %s\n", item instanceof Error );
            System.out.printf( "is warning %s\n", item instanceof Warning );
        }
    }

    @Disabled( "TODO(TECH-880-spike) how to make a clean API but keeping internal structure" )
    @Test
    void howToReturnInterfaceReportItemButStorePerConcreteType()
    {
        Error err = new Error();
        Warning warn = new Warning();
        Report report = new Report();
        ReportItem item = new Error();
        report.add( err );
        report.add( warn );
        report.add( item );
        report.add( (ReportItem) warn );
    }

}
