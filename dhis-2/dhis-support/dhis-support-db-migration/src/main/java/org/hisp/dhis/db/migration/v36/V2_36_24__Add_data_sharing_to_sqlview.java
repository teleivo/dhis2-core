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
package org.hisp.dhis.db.migration.v36;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.hisp.dhis.user.sharing.Sharing;
import org.hisp.dhis.util.SharingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jan Bernitt
 */
public class V2_36_24__Add_data_sharing_to_sqlview extends BaseJavaMigration {

  private static final Logger log =
      LoggerFactory.getLogger(V2_36_24__Add_data_sharing_to_sqlview.class);

  @Override
  public void migrate(Context context) throws Exception {
    try (Statement statement = context.getConnection().createStatement()) {
      ResultSet results = statement.executeQuery("select sqlviewid, sharing from sqlview;");
      while (results.next()) {
        updateRow(context, results.getLong(1), results.getString(2));
      }
    }
  }

  private void updateRow(Context context, long sqlviewid, String sharing)
      throws SQLException, JsonProcessingException {
    String updatedSharing = SharingUtils.withAccess(sharing, Sharing::copyMetadataToData);
    try (PreparedStatement statement =
        context
            .getConnection()
            .prepareStatement("update sqlview set sharing = ?::json where sqlviewid = ?")) {
      statement.setLong(2, sqlviewid);
      statement.setString(1, updatedSharing);

      log.info("Executing sharing migration query: [" + statement + "]");
      statement.executeUpdate();
    } catch (SQLException e) {
      log.error(e.getMessage());
      throw e;
    }
  }
}
