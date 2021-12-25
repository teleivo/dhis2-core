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

import java.util.List;

import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.TrackerDto;
import org.hisp.dhis.tracker.validation.spike.one.hooks.AssignedUserValidationHook;
import org.hisp.dhis.tracker.validation.spike.one.hooks.EnrollmentDateValidationHook;
import org.hisp.dhis.tracker.validation.spike.one.hooks.UidValidationHook;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration( "trackerSpikeOneImportValidationConfig" )
public class SpikeValidationServiceConfig
{

    // TODO write an integration style test to show this works
    // TODO use Spring components?
    // TODO implement a few more challenging hooks. what about the one that only
    // implements a hook for 3/4 entities?
    @Bean
    public List<ValidationHook<TrackerDto>> preCheckValidationHooks()
    {
        return List.of( new UidValidationHook() );
    }

    @Bean
    public List<ValidationHook<Event>> eventValidationHooks()
    {
        return List.of(
            AssignedUserValidationHook::validateUserInPreheat,
            AssignedUserValidationHook::validateUserAssignmentIsEnabled );
    }

    @Bean
    public List<ValidationHook<Enrollment>> enrollmentValidationHooks()
    {
        return List.of(
            EnrollmentDateValidationHook::validateMandatoryDates,
            EnrollmentDateValidationHook::validateEnrollmentAtNotInFuture,
            EnrollmentDateValidationHook::validateEnrolledAtNotInFuture,
            EnrollmentDateValidationHook::validateOccurredAt );
    }
}
