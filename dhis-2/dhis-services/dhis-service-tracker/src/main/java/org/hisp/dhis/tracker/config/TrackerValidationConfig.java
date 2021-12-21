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
package org.hisp.dhis.tracker.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hisp.dhis.tracker.validation.TrackerValidationHook;
import org.hisp.dhis.tracker.validation.hooks.AssignedUserValidationHook;
import org.hisp.dhis.tracker.validation.hooks.EnrollmentAttributeValidationHook;
import org.hisp.dhis.tracker.validation.hooks.EnrollmentDateValidationHook;
import org.hisp.dhis.tracker.validation.hooks.EnrollmentGeoValidationHook;
import org.hisp.dhis.tracker.validation.hooks.EnrollmentInExistingValidationHook;
import org.hisp.dhis.tracker.validation.hooks.EnrollmentNoteValidationHook;
import org.hisp.dhis.tracker.validation.hooks.EnrollmentRuleValidationHook;
import org.hisp.dhis.tracker.validation.hooks.EventCategoryOptValidationHook;
import org.hisp.dhis.tracker.validation.hooks.EventDataValuesValidationHook;
import org.hisp.dhis.tracker.validation.hooks.EventDateValidationHook;
import org.hisp.dhis.tracker.validation.hooks.EventGeoValidationHook;
import org.hisp.dhis.tracker.validation.hooks.EventNoteValidationHook;
import org.hisp.dhis.tracker.validation.hooks.EventRuleValidationHook;
import org.hisp.dhis.tracker.validation.hooks.PreCheckDataRelationsValidationHook;
import org.hisp.dhis.tracker.validation.hooks.PreCheckExistenceValidationHook;
import org.hisp.dhis.tracker.validation.hooks.PreCheckMandatoryFieldsValidationHook;
import org.hisp.dhis.tracker.validation.hooks.PreCheckMetaValidationHook;
import org.hisp.dhis.tracker.validation.hooks.PreCheckSecurityOwnershipValidationHook;
import org.hisp.dhis.tracker.validation.hooks.PreCheckUidValidationHook;
import org.hisp.dhis.tracker.validation.hooks.PreCheckUpdatableFieldsValidationHook;
import org.hisp.dhis.tracker.validation.hooks.RelationshipsValidationHook;
import org.hisp.dhis.tracker.validation.hooks.RepeatedEventsValidationHook;
import org.hisp.dhis.tracker.validation.hooks.TrackedEntityAttributeValidationHook;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;

/**
 * Configuration class for the tracker importer validation hook ordering. The
 * Hooks will be run in the same order they appear in this class.
 *
 * @author Luca Cambi <luca@dhis2.org>
 */
@Configuration( "trackerImportValidationConfig" )
public class TrackerValidationConfig
{
    private final Map<Class<? extends TrackerValidationHook>, TrackerValidationHook> validationHooks;

    public TrackerValidationConfig( Collection<TrackerValidationHook> checkers )
    {
        validationHooks = byClass( checkers );
    }

    @SuppressWarnings( "unchecked" )
    private <T> Map<Class<? extends T>, T> byClass( Collection<T> items )
    {
        return items.stream()
            .collect( Collectors.toMap(
                e -> (Class<? extends T>) e.getClass(),
                Functions.identity() ) );
    }

    @Bean
    public List<TrackerValidationHook> ruleEngineValidationHooks()
    {
        return ImmutableList.of(
            getHookByClass( EnrollmentRuleValidationHook.class ),
            getHookByClass( EventRuleValidationHook.class ),
            getHookByClass( TrackedEntityAttributeValidationHook.class ),
            getHookByClass( EnrollmentAttributeValidationHook.class ),
            getHookByClass( EventDataValuesValidationHook.class ) );
    }

    @Bean
    public List<TrackerValidationHook> validationHooks()
    {
        return ImmutableList.of(
            getHookByClass( PreCheckUidValidationHook.class ),
            getHookByClass( PreCheckExistenceValidationHook.class ),
            getHookByClass( PreCheckMandatoryFieldsValidationHook.class ),
            getHookByClass( PreCheckMetaValidationHook.class ),
            getHookByClass( PreCheckUpdatableFieldsValidationHook.class ),
            getHookByClass( PreCheckDataRelationsValidationHook.class ),
            getHookByClass( PreCheckSecurityOwnershipValidationHook.class ),

            getHookByClass( TrackedEntityAttributeValidationHook.class ),

            getHookByClass( EnrollmentNoteValidationHook.class ),
            getHookByClass( EnrollmentInExistingValidationHook.class ),
            getHookByClass( EnrollmentGeoValidationHook.class ),
            getHookByClass( EnrollmentDateValidationHook.class ),
            getHookByClass( EnrollmentAttributeValidationHook.class ),

            getHookByClass( EventCategoryOptValidationHook.class ),
            getHookByClass( EventDateValidationHook.class ),
            getHookByClass( EventGeoValidationHook.class ),
            getHookByClass( EventNoteValidationHook.class ),
            getHookByClass( EventDataValuesValidationHook.class ),

            getHookByClass( RelationshipsValidationHook.class ),

            getHookByClass( AssignedUserValidationHook.class ),

            getHookByClass( RepeatedEventsValidationHook.class ) // This
                                                                 // validation
                                                                 // must be run
        // after
        // all the Event validations
        // because it needs to consider all and only the valid events
        );
    }

    private TrackerValidationHook getHookByClass( Class<? extends TrackerValidationHook> hookClass )
    {
        return Optional.ofNullable( validationHooks.get( hookClass ) )
            .orElseThrow(
                () -> new IllegalArgumentException( "Unable to find validation hook by class: " + hookClass ) );
    }

}
