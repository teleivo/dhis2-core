/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without
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
package org.hisp.dhis.webapi.controller.tracker.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hisp.dhis.common.OpenApi;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.program.EnrollmentStatus;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.locationtech.jts.geom.Geometry;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@OpenApi.Shared(name = "TrackerEnrollment")
@OpenApi.Identifiable(as = org.hisp.dhis.program.Enrollment.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
  @OpenApi.Property({UID.class, org.hisp.dhis.program.Enrollment.class})
  @JsonProperty
  private UID enrollment;

  @JsonProperty private Instant createdAt;

  @JsonProperty private Instant createdAtClient;

  @JsonProperty private Instant updatedAt;

  @JsonProperty private Instant updatedAtClient;

  @OpenApi.Property({UID.class, TrackedEntity.class})
  @JsonProperty
  private UID trackedEntity;

  @JsonProperty private String program;

  @JsonProperty @Builder.Default private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

  @JsonProperty private String orgUnit;

  @JsonProperty private Instant enrolledAt;

  @JsonProperty private Instant occurredAt;

  @JsonProperty private boolean followUp;

  @JsonProperty private String completedBy;

  @JsonProperty private Instant completedAt;

  @JsonProperty private boolean deleted;

  @JsonProperty private String storedBy;

  @JsonProperty private User createdBy;

  @JsonProperty private User updatedBy;

  @JsonProperty private Geometry geometry;

  @JsonProperty @Builder.Default private List<Event> events = new ArrayList<>();

  @JsonProperty @Builder.Default private List<Relationship> relationships = new ArrayList<>();

  @JsonProperty @Builder.Default private List<Attribute> attributes = new ArrayList<>();

  @JsonProperty @Builder.Default private List<Note> notes = new ArrayList<>();
}
