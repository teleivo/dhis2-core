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
package org.hisp.dhis.tracker.imports.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hisp.dhis.common.UID;

@Getter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackerObjects implements Serializable {
  @JsonProperty @Builder.Default
  private final List<TrackedEntity> trackedEntities = new ArrayList<>();

  @JsonProperty @Builder.Default private final List<Enrollment> enrollments = new ArrayList<>();
  @JsonProperty @Builder.Default private final List<Event> events = new ArrayList<>();
  @JsonProperty @Builder.Default private final List<Relationship> relationships = new ArrayList<>();

  public Optional<TrackedEntity> findTrackedEntity(@Nonnull UID uid) {
    return find(this.trackedEntities, uid);
  }

  public Optional<Enrollment> findEnrollment(@Nonnull UID uid) {
    return find(this.enrollments, uid);
  }

  public Optional<Event> findEvent(@Nonnull UID uid) {
    return find(this.events, uid);
  }

  public Optional<Relationship> findRelationship(@Nonnull UID uid) {
    return find(this.relationships, uid);
  }

  private static <T extends TrackerDto> Optional<T> find(List<T> entities, UID uid) {
    return entities.stream().filter(e -> Objects.equals(e.getUid(), uid)).findFirst();
  }
}
