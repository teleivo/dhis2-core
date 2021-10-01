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
package org.hisp.dhis.tracker.job;

import lombok.Builder;
import lombok.Data;

import org.hisp.dhis.artemis.MessageType;
import org.hisp.dhis.artemis.SerializableMessage;
import org.hisp.dhis.tracker.TrackerImportParams;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Used by Apache Artemis to pass tracker import jobs from the /api/tracker
 * endpoint to the tracker import services.
 *
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Data
@Builder( builderClassName = "TrackerMessageBuilder" )
@JsonDeserialize( builder = TrackerMessage.TrackerMessageBuilder.class )
public class TrackerMessage implements SerializableMessage
{
    @JsonProperty
    private final String uid;

    @JsonProperty
    private String authentication;

    @JsonProperty
    private final TrackerImportParams trackerImportParams;

    @Override
    public MessageType getMessageType()
    {
        return MessageType.TRACKER_JOB;
    }

    @JsonPOJOBuilder( withPrefix = "" )
    public static final class TrackerMessageBuilder
    {
    }
}
