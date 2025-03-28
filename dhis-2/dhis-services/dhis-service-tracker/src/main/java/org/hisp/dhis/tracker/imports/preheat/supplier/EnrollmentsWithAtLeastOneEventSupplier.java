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
package org.hisp.dhis.tracker.imports.preheat.supplier;

import java.util.ArrayList;
import java.util.List;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.tracker.imports.domain.TrackerObjects;
import org.hisp.dhis.tracker.imports.preheat.TrackerPreheat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * This supplier adds to the pre-heat object a List of all Enrollment UIDs that have at least ONE
 * event that is not logically deleted ('deleted = true').
 *
 * @author Luciano Fiandesio
 */
@Component
public class EnrollmentsWithAtLeastOneEventSupplier extends JdbcAbstractPreheatSupplier {
  private static final String COLUMN = "uid";

  private static final String SQL =
      "select  "
          + COLUMN
          + " from enrollment "
          + "where exists( select eventid "
          + "from event "
          + "where enrollment.enrollmentid = event.enrollmentid "
          + "and enrollment.deleted = false) "
          + "and enrollmentid in (:ids)";

  protected EnrollmentsWithAtLeastOneEventSupplier(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  @Override
  public void preheatAdd(TrackerObjects trackerObjects, TrackerPreheat preheat) {
    List<Long> enrollmentIds =
        preheat.getEnrollments().values().stream().map(IdentifiableObject::getId).toList();

    if (!enrollmentIds.isEmpty()) {
      List<UID> uids = new ArrayList<>();

      MapSqlParameterSource parameters = new MapSqlParameterSource();
      parameters.addValue("ids", enrollmentIds);
      jdbcTemplate.query(
          SQL,
          parameters,
          rs -> {
            uids.add(UID.of(rs.getString(COLUMN)));
          });
      preheat.setEnrollmentsWithOneOrMoreNonDeletedEvent(uids);
    }
  }
}
