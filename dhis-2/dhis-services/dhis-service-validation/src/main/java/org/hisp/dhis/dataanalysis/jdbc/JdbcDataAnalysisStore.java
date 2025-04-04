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
package org.hisp.dhis.dataanalysis.jdbc;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.commons.collection.PaginatedList;
import org.hisp.dhis.commons.util.TextUtils;
import org.hisp.dhis.dataanalysis.DataAnalysisMeasures;
import org.hisp.dhis.dataanalysis.DataAnalysisStore;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.datavalue.DeflatedDataValue;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.system.objectmapper.DeflatedDataValueNameMinMaxRowMapper;
import org.hisp.dhis.util.DateUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

/**
 * @author Lars Helge Overland
 * @author Halvdan Hoem Grelland
 */
@Slf4j
@RequiredArgsConstructor
@Repository("org.hisp.dhis.dataanalysis.DataAnalysisStore")
public class JdbcDataAnalysisStore implements DataAnalysisStore {

  @Qualifier("readOnlyJdbcTemplate")
  private final JdbcTemplate jdbcTemplate;

  @Override
  public List<DataAnalysisMeasures> getDataAnalysisMeasures(
      DataElement dataElement,
      Collection<CategoryOptionCombo> categoryOptionCombos,
      OrganisationUnit orgUnit,
      Date from) {
    if (categoryOptionCombos.isEmpty() || dataElement == null || orgUnit == null) {
      return List.of();
    }

    List<DataAnalysisMeasures> measures = new ArrayList<>();

    String catOptionComboIds =
        TextUtils.getCommaDelimitedString(getIdentifiers(categoryOptionCombos));

    String sql =
        "select dv.sourceid, dv.categoryoptioncomboid, "
            + "avg(cast(dv.value as double precision)) as average, "
            + "stddev_pop(cast(dv.value as double precision)) as standarddeviation "
            + "from datavalue dv "
            + "inner join period pe on dv.periodid = pe.periodid "
            + "where dv.dataelementid = "
            + dataElement.getId()
            + " "
            + "and dv.categoryoptioncomboid in ("
            + catOptionComboIds
            + ") "
            + "and pe.startdate >= '"
            + DateUtils.toMediumDate(from)
            + "' "
            + "and dv.sourceid = "
            + orgUnit.getId()
            + " "
            + "and dv.deleted is false "
            + "group by dv.sourceid, dv.categoryoptioncomboid;";

    SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);

    while (rowSet.next()) {
      int orgUnitId = rowSet.getInt(1);
      int categoryOptionComboId = rowSet.getInt(2);
      double average = rowSet.getDouble(3);
      double stdDev = rowSet.getDouble(4);

      if (stdDev != 0.0) {
        measures.add(new DataAnalysisMeasures(orgUnitId, categoryOptionComboId, average, stdDev));
      }
    }

    return measures;
  }

  @Override
  public List<DeflatedDataValue> getMinMaxViolations(
      Collection<DataElement> dataElements,
      Collection<CategoryOptionCombo> categoryOptionCombos,
      Collection<Period> periods,
      OrganisationUnit orgUnit,
      int limit) {
    if (dataElements.isEmpty()
        || categoryOptionCombos.isEmpty()
        || periods.isEmpty()
        || orgUnit == null) {
      return List.of();
    }

    String dataElementIds = getCommaDelimitedString(getIdentifiers(dataElements));
    String periodIds = getCommaDelimitedString(getIdentifiers(periods));
    String categoryOptionComboIds = getCommaDelimitedString(getIdentifiers(categoryOptionCombos));

    String sql =
        "select dv.dataelementid, dv.periodid, dv.sourceid, dv.categoryoptioncomboid, dv.attributeoptioncomboid, "
            + "dv.value, dv.storedby, dv.lastupdated, "
            + "dv.created, dv.comment, dv.followup, ou.name as sourcename, de.name as dataelementname, "
            + "pt.name as periodtypename, pe.startdate, pe.enddate, coc.name as categoryoptioncomboname, "
            + "mm.minimumvalue, mm.maximumvalue "
            + "from datavalue dv "
            + "left join minmaxdataelement mm on dv.dataelementid = mm.dataelementid "
            + "and dv.categoryoptioncomboid = mm.categoryoptioncomboid and dv.sourceid = mm.sourceid "
            + "inner join dataelement de on dv.dataelementid = de.dataelementid "
            + "inner join period pe on dv.periodid = pe.periodid "
            + "inner join periodtype pt on pe.periodtypeid = pt.periodtypeid "
            + "inner join organisationunit ou on dv.sourceid = ou.organisationunitid "
            + "inner join categoryoptioncombo coc on dv.categoryoptioncomboid = coc.categoryoptioncomboid "
            + "where dv.dataelementid in ("
            + dataElementIds
            + ") "
            + "and dv.categoryoptioncomboid in ("
            + categoryOptionComboIds
            + ") "
            + "and dv.periodid in ("
            + periodIds
            + ") "
            + "and dv.sourceid = "
            + orgUnit.getId()
            + " "
            + "and ("
            + "cast(dv.value as double precision) < mm.minimumvalue "
            + "or cast(dv.value as double precision) > mm.maximumvalue) "
            + "and dv.deleted is false "
            + "limit "
            + limit
            + ";";

    return jdbcTemplate.query(sql, new DeflatedDataValueNameMinMaxRowMapper(null, null));
  }

  @Override
  public List<DeflatedDataValue> getDeflatedDataValues(
      DataElement dataElement,
      CategoryOptionCombo categoryOptionCombo,
      Collection<Period> periods,
      Map<Long, Integer> lowerBoundMap,
      Map<Long, Integer> upperBoundMap) {
    if (lowerBoundMap == null || lowerBoundMap.isEmpty() || periods.isEmpty()) {
      return List.of();
    }

    List<List<Long>> organisationUnitPages =
        new PaginatedList<>(lowerBoundMap.keySet()).setPageSize(1000).getPages();

    log.debug("No of pages: " + organisationUnitPages.size());

    List<DeflatedDataValue> dataValues = new ArrayList<>();

    for (List<Long> unitPage : organisationUnitPages) {
      dataValues.addAll(
          getDeflatedDataValues(
              dataElement, categoryOptionCombo, periods, unitPage, lowerBoundMap, upperBoundMap));
    }

    return dataValues;
  }

  private List<DeflatedDataValue> getDeflatedDataValues(
      DataElement dataElement,
      CategoryOptionCombo categoryOptionCombo,
      Collection<Period> periods,
      List<Long> organisationUnits,
      Map<Long, Integer> lowerBoundMap,
      Map<Long, Integer> upperBoundMap) {
    String periodIds = TextUtils.getCommaDelimitedString(getIdentifiers(periods));

    String sql =
        "select dv.dataelementid, dv.periodid, dv.sourceid, "
            + "dv.categoryoptioncomboid, dv.attributeoptioncomboid, dv.value, dv.storedby, dv.lastupdated, "
            + "dv.created, dv.comment, dv.followup, ou.name as sourcename, "
            + "? as dataelementname, pt.name as periodtypename, pe.startdate, pe.enddate, "
            + "? as categoryoptioncomboname "
            + "from datavalue dv "
            + "inner join period pe on dv.periodid = pe.periodid "
            + "inner join periodtype pt on pe.periodtypeid = pt.periodtypeid "
            + "inner join organisationunit ou on dv.sourceid = ou.organisationunitid "
            + "where dv.dataelementid = "
            + dataElement.getId()
            + " "
            + "and dv.categoryoptioncomboid = "
            + categoryOptionCombo.getId()
            + " "
            + "and dv.periodid in ("
            + periodIds
            + ") and (";

    for (Long orgUnitUid : organisationUnits) {
      sql +=
          "(dv.sourceid = "
              + orgUnitUid
              + " "
              + "and (cast( dv.value as double precision) < "
              + lowerBoundMap.get(orgUnitUid)
              + " "
              + "or cast(dv.value as double precision) > "
              + upperBoundMap.get(orgUnitUid)
              + ")) or ";
    }

    sql = TextUtils.removeLastOr(sql) + ") ";
    sql += "and dv.deleted is false ";

    PreparedStatementSetter pss =
        (ps) -> {
          ps.setString(1, dataElement.getName());
          ps.setString(2, categoryOptionCombo.getName());
        };

    return jdbcTemplate.query(
        sql, pss, new DeflatedDataValueNameMinMaxRowMapper(lowerBoundMap, upperBoundMap));
  }

  @Override
  public List<DeflatedDataValue> getFollowupDataValues(
      Collection<DataElement> dataElements,
      Collection<CategoryOptionCombo> categoryOptionCombos,
      Collection<Period> periods,
      OrganisationUnit orgUnit,
      int limit) {
    if (dataElements.isEmpty()
        || categoryOptionCombos.isEmpty()
        || periods.isEmpty()
        || orgUnit == null) {
      return List.of();
    }

    String dataElementIds = getCommaDelimitedString(getIdentifiers(dataElements));
    String periodIds = getCommaDelimitedString(getIdentifiers(periods));
    String categoryOptionComboIds = getCommaDelimitedString(getIdentifiers(categoryOptionCombos));

    String sql =
        "select dv.dataelementid, dv.periodid, dv.sourceid, "
            + "dv.categoryoptioncomboid, dv.attributeoptioncomboid, dv.value, dv.storedby, dv.lastupdated, "
            + "dv.created, dv.comment, dv.followup, ou.name as sourcename, de.name as dataelementname, "
            + "pt.name as periodtypename, pe.startdate, pe.enddate, coc.name as categoryoptioncomboname, "
            + "mm.minimumvalue, mm.maximumvalue "
            + "from datavalue dv "
            + "left join minmaxdataelement mm on dv.dataelementid = mm.dataelementid "
            + "and dv.categoryoptioncomboid = mm.categoryoptioncomboid and dv.sourceid = mm.sourceid "
            + "inner join dataelement de on dv.dataelementid = de.dataelementid "
            + "inner join period pe on dv.periodid = pe.periodid "
            + "inner join periodtype pt on pe.periodtypeid = pt.periodtypeid "
            + "inner join organisationunit ou on dv.sourceid = ou.organisationunitid "
            + "inner join categoryoptioncombo coc on dv.categoryoptioncomboid = coc.categoryoptioncomboid "
            + "where dv.dataelementid in ("
            + dataElementIds
            + ") "
            + "and dv.categoryoptioncomboid in ("
            + categoryOptionComboIds
            + ") "
            + "and dv.periodid in ("
            + periodIds
            + ") "
            + "and dv.sourceid = "
            + orgUnit.getId()
            + " "
            + "and dv.followup = true and dv.deleted is false "
            + "limit "
            + limit
            + ";";

    return jdbcTemplate.query(sql, new DeflatedDataValueNameMinMaxRowMapper(null, null));
  }
}
