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
package org.hisp.dhis.tracker.validation.spike.one;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.tracker.TrackerType;
import org.hisp.dhis.tracker.ValidationMode;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.hisp.dhis.tracker.report.TrackerErrorReport;
import org.hisp.dhis.tracker.report.TrackerValidationReport;
import org.hisp.dhis.tracker.validation.spike.one.hooks.AssignedUserValidationHook;
import org.hisp.dhis.tracker.validation.spike.one.hooks.EnrollmentDateValidationHook;
import org.hisp.dhis.tracker.validation.spike.one.hooks.UidValidationHook;
import org.hisp.dhis.user.User;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class SpikeOneValidationServiceTest
{

    @Test
    void reportAndRemoveInvalidFromBundleWithRemoveOnErrorHook()
    {
        TrackerPreheat preheat = mock( TrackerPreheat.class );

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
        when( preheat.get( User.class, validEvent.getAssignedUser() ) ).thenReturn( new User() );
        when( preheat.get( User.class, eventWithInvalidUid.getAssignedUser() ) ).thenReturn( new User() );
        when( preheat.get( User.class, eventWithInvalidAssignedUser.getAssignedUser() ) ).thenReturn( null );
        ProgramStage programStage = new ProgramStage();
        programStage.setEnableUserAssignment( true );
        when( preheat.get( eq( ProgramStage.class ), any() ) ).thenReturn( programStage );

        Enrollment validEnrollment = enrollment();
        // invalid because enrolledAt is in the future despite program
        // selectEnrollmentDatesInFuture == false
        Enrollment enrollmentWithInvalidEnrolledAt = enrollment();
        Program program1 = mock( Program.class );
        when( program1.getSelectEnrollmentDatesInFuture() ).thenReturn( false );
        when( preheat.get( Program.class, enrollmentWithInvalidEnrolledAt.getProgram() ) ).thenReturn( program1 );
        Program program2 = mock( Program.class );
        when( program2.getSelectEnrollmentDatesInFuture() ).thenReturn( true );
        when( preheat.get( Program.class, validEnrollment.getProgram() ) ).thenReturn( program2 );
        List<Enrollment> enrollments = new ArrayList<>();
        enrollments.add( validEnrollment );
        enrollments.add( enrollmentWithInvalidEnrolledAt );

        TrackerBundle bundle = TrackerBundle.builder()
            .validationMode( ValidationMode.FULL )
            .skipRuleEngine( true )
            .preheat( preheat )
            .events( events )
            .enrollments( enrollments )
            .build();

        ValidationService validationService = new SpikeOneValidationService(
            List.of( new UidValidationHook() ),
            List.of( AssignedUserValidationHook::validateUserInPreheat,
                AssignedUserValidationHook::validateUserAssignmentIsEnabled ),
            List.of( EnrollmentDateValidationHook::validateMandatoryDates,
                EnrollmentDateValidationHook::validateEnrolledAtNotInFuture ) );

        TrackerValidationReport report = validationService.validate( bundle );

        assertTrue( report.hasErrors() );
        assertEquals( 3, report.getErrorReports().size() );
        assertTrue( report.hasErrorReport( err -> TrackerErrorCode.E1048 == err.getErrorCode()
            && TrackerType.EVENT == err.getTrackerType()
            && eventWithInvalidUid.getUid().equals( err.getUid() ) ) );
        assertTrue( report.hasErrorReport( err -> TrackerErrorCode.E1118 == err.getErrorCode()
            && TrackerType.EVENT == err.getTrackerType()
            && eventWithInvalidAssignedUser.getUid().equals( err.getUid() ) ) );
        assertTrue( report.hasErrorReport( err -> TrackerErrorCode.E1020 == err.getErrorCode()
            && TrackerType.ENROLLMENT == err.getTrackerType()
            && enrollmentWithInvalidEnrolledAt.getUid().equals( err.getUid() ) ) );

        assertFalse( bundle.getEvents().contains( eventWithInvalidUid ) );
        assertFalse( bundle.getEvents().contains( eventWithInvalidAssignedUser ) );
        assertTrue( bundle.getEvents().contains( validEvent ) );
        assertFalse( bundle.getEnrollments().contains( enrollmentWithInvalidEnrolledAt ) );
        assertTrue( bundle.getEnrollments().contains( validEnrollment ) );
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
        enrollment.setEnrolledAt( Instant.now().plus( 1, ChronoUnit.DAYS ) );
        enrollment.setProgram( CodeGenerator.generateUid() );
        return enrollment;
    }

    @Test
    void failFastDoesNotCallHooksAfterFailure()
    {
        TrackerPreheat preheat = mock( TrackerPreheat.class );

        Event validEvent = event();
        Event eventWithInvalidUid = event();
        List<Event> events = new ArrayList<>();
        events.add( eventWithInvalidUid );
        events.add( validEvent );

        List<Enrollment> enrollments = new ArrayList<>();
        enrollments.add( enrollment() );

        TrackerBundle bundle = TrackerBundle.builder()
            .validationMode( ValidationMode.FAIL_FAST )
            .skipRuleEngine( true )
            .preheat( preheat )
            .events( events )
            .enrollments( enrollments )
            .build();

        ValidationHook preCheckHook1 = mock( ValidationHook.class );
        when( preCheckHook1.validate( bundle, eventWithInvalidUid ) )
            .thenReturn( Optional.of( TrackerErrorReport.builder()
                .errorCode( TrackerErrorCode.E1048 )
                .uid( eventWithInvalidUid.getUid() )
                .trackerType( TrackerType.EVENT )
                .build( bundle ) ) );
        ValidationHook preCheckHook2 = mock( ValidationHook.class );
        ValidationHook eventHook = mock( ValidationHook.class );
        ValidationHook enrollmentHook = mock( ValidationHook.class );
        ValidationService validationService = new SpikeOneValidationService(
            List.of(
                preCheckHook1,
                preCheckHook2 ),
            List.of( eventHook ),
            List.of( enrollmentHook ) );

        TrackerValidationReport report = validationService.validate( bundle );

        assertTrue( report.hasErrors() );
        assertEquals( 1, report.getErrorReports().size() );
        assertTrue( report.hasErrorReport( err -> TrackerErrorCode.E1048 == err.getErrorCode()
            && TrackerType.EVENT == err.getTrackerType()
            && eventWithInvalidUid.getUid().equals( err.getUid() ) ) );

        assertFalse( bundle.getEvents().contains( eventWithInvalidUid ) );
        assertTrue( bundle.getEvents().contains( validEvent ) );

        verify( preCheckHook1, times( 1 ) ).validate( bundle, eventWithInvalidUid );
        verifyNoMoreInteractions( preCheckHook1 );
        verifyNoInteractions( preCheckHook2 );
        verifyNoInteractions( eventHook );
        verifyNoInteractions( enrollmentHook );
    }

    @Test
    void play()
    {
        // TrackerBundle bundle = mock(TrackerBundle.class);
        // TrackerWarningReport warn1 = TrackerWarningReport.builder()
        // .warningCode( TrackerErrorCode.E1032 )
        // .trackerType( TrackerType.EVENT )
        // .uid( CodeGenerator.generateUid() ).build( bundle);
        // TrackerErrorReport err1 = TrackerErrorReport.builder()
        // .errorCode( TrackerErrorCode.E1032 )
        // .trackerType( TrackerType.EVENT )
        // .uid( CodeGenerator.generateUid() ).build( bundle);
        // List<Optional<TrackerReportItem>> items = List.of(
        // Optional.of(warn1),
        // Optional.of(err1)
        // );
        // how would I check if there is an error? in the list of optionals?
        // could I use ifPresent to add the errors? or better collect them end
        // add all at once?
        // items.stream()

        // TODO how would I compose validations into a hook that returns a
        // List<Optional<ReportItem>>?
    }
}
