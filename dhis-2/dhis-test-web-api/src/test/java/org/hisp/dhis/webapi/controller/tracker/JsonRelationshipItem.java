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
package org.hisp.dhis.webapi.controller.tracker;

import org.hisp.dhis.jsontree.JsonList;
import org.hisp.dhis.jsontree.JsonObject;

/** Representation of {@link org.hisp.dhis.webapi.controller.tracker.view.RelationshipItem}. */
public interface JsonRelationshipItem extends JsonObject {
  default JsonTrackedEntity getTrackedEntity() {
    return get("trackedEntity").as(JsonTrackedEntity.class);
  }

  default JsonEnrollment getEnrollment() {
    return get("enrollment").as(JsonEnrollment.class);
  }

  default JsonEvent getEvent() {
    return get("event").as(JsonEvent.class);
  }

  interface JsonTrackedEntity extends JsonObject {
    default String getTrackedEntity() {
      return getString("trackedEntity").string();
    }

    default String getTrackedEntityType() {
      return getString("trackedEntityType").string();
    }

    default String getOrgUnit() {
      return getString("orgUnit").string();
    }

    default JsonList<JsonAttribute> getAttributes() {
      return get("attributes").asList(JsonAttribute.class);
    }

    default JsonList<JsonEnrollment> getEnrollments() {
      return get("enrollments").asList(JsonEnrollment.class);
    }

    default JsonList<JsonProgramOwner> getProgramOwners() {
      return get("programOwners").asList(JsonProgramOwner.class);
    }
  }

  interface JsonEnrollment extends JsonObject {
    default String getEnrollment() {
      return getString("enrollment").string();
    }

    default String getTrackedEntity() {
      return getString("trackedEntity").string();
    }

    default String getProgram() {
      return getString("program").string();
    }

    default String getOrgUnit() {
      return getString("orgUnit").string();
    }

    default JsonList<JsonEvent> getEvents() {
      return get("events").asList(JsonEvent.class);
    }

    default JsonList<JsonAttribute> getAttributes() {
      return get("attributes").asList(JsonAttribute.class);
    }

    default JsonList<JsonNote> getNotes() {
      return get("notes").asList(JsonNote.class);
    }

    default Boolean getFollowUp() {
      return getBoolean("followUp").bool();
    }
  }

  interface JsonEvent extends JsonObject {
    default String getEvent() {
      return getString("event").string();
    }

    default String getStatus() {
      return getString("status").string();
    }

    default String getProgramStage() {
      return getString("programStage").string();
    }

    default String getEnrollment() {
      return getString("enrollment").string();
    }

    default JsonUser getAssignedUser() {
      return get("assignedUser").as(JsonUser.class);
    }

    default JsonList<JsonDataValue> getDataValues() {
      return get("dataValues").asList(JsonDataValue.class);
    }

    default JsonList<JsonNote> getNotes() {
      return get("notes").asList(JsonNote.class);
    }

    default Boolean getFollowUp() {
      return getBoolean("followUp").bool();
    }

    default Boolean getLegacyFollowUp() {
      return getBoolean("followup").bool();
    }
  }
}
