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
package org.hisp.dhis.analytics.event.data;

import static org.hisp.dhis.analytics.AnalyticsConstants.ANALYTICS_TBL_ALIAS;
import static org.hisp.dhis.common.DimensionalObject.PERIOD_DIM_ID;
import static org.hisp.dhis.commons.util.TextUtils.EMPTY;
import static org.hisp.dhis.util.DateUtils.plusOneDay;
import static org.hisp.dhis.util.DateUtils.toMediumDate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.analytics.EventOutputType;
import org.hisp.dhis.analytics.TimeField;
import org.hisp.dhis.analytics.event.EventQueryParams;
import org.hisp.dhis.common.DimensionalItemObject;
import org.hisp.dhis.db.sql.SqlBuilder;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.program.AnalyticsPeriodBoundary;
import org.hisp.dhis.program.AnalyticsType;
import org.hisp.dhis.program.ProgramIndicator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
class EnrollmentTimeFieldSqlRenderer extends TimeFieldSqlRenderer {

  public EnrollmentTimeFieldSqlRenderer(SqlBuilder sqlBuilder) {
    super(sqlBuilder);
  }

  @Getter private final Set<TimeField> allowedTimeFields = Set.of(TimeField.LAST_UPDATED);

  @Override
  protected String getAggregatedConditionForPeriods(EventQueryParams params) {
    List<DimensionalItemObject> periods = params.getAllDimensionOrFilterItems(PERIOD_DIM_ID);

    Optional<TimeField> timeField = getTimeField(params);

    StringBuilder sql = new StringBuilder();

    if (timeField.isPresent()) {
      return sql.append(
              periods.stream()
                  .filter(this::isPeriod)
                  .map(dimensionalItemObject -> (Period) dimensionalItemObject)
                  .map(period -> toSqlCondition(period, timeField.get()))
                  .collect(Collectors.joining(" or ", "(", ")")))
          .toString();
    }
    return sql.append(getSqlForAllPeriods(ANALYTICS_TBL_ALIAS, periods)).toString();
  }

  @Override
  protected String getColumnName(Optional<TimeField> timeField, EventOutputType outputType) {
    return timeField.orElse(TimeField.ENROLLMENT_DATE).getEnrollmentColumnName();
  }

  @Override
  protected String getConditionForNonDefaultBoundaries(EventQueryParams params) {
    String sql =
        params.getProgramIndicator().getAnalyticsPeriodBoundaries().stream()
            .filter(
                boundary ->
                    boundary.isCohortDateBoundary()
                        && !boundary.isEnrollmentHavingEventDateCohortBoundary())
            .map(
                boundary ->
                    statementBuilder.getBoundaryCondition(
                        boundary,
                        params.getProgramIndicator(),
                        params.getTimeFieldAsField(AnalyticsType.ENROLLMENT),
                        params.getEarliestStartDate(),
                        params.getLatestEndDate()))
            .collect(Collectors.joining(" and "));

    String sqlEventCohortBoundary =
        params.getProgramIndicator().hasEventDateCohortBoundary()
            ? getProgramIndicatorEventInProgramStageSql(
                params.getProgramIndicator(),
                params.getEarliestStartDate(),
                params.getLatestEndDate())
            : EMPTY;

    return Stream.of(sql, sqlEventCohortBoundary)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(" and "));
  }

  private String getProgramIndicatorEventInProgramStageSql(
      ProgramIndicator programIndicator, Date reportingStartDate, Date reportingEndDate) {
    Assert.isTrue(
        programIndicator.hasEventDateCohortBoundary(),
        "Can not get event date cohort boundaries for program indicator:"
            + programIndicator.getUid());

    Map<String, Set<AnalyticsPeriodBoundary>> map =
        programIndicator.getEventDateCohortBoundaryByProgramStage();

    SimpleDateFormat format = new SimpleDateFormat();
    format.applyPattern(Period.DEFAULT_DATE_FORMAT);

    String sql = EMPTY;
    for (String programStage : map.keySet()) {
      Set<AnalyticsPeriodBoundary> boundaries = map.get(programStage);

      String eventTableName = "analytics_event_" + programIndicator.getProgram().getUid();
      sql +=
          " exists(select 1 from "
              + eventTableName
              + " where "
              + eventTableName
              + ".enrollment = "
              + ANALYTICS_TBL_ALIAS
              + ".enrollment and occurreddate is not null ";

      for (AnalyticsPeriodBoundary boundary : boundaries) {
        sql +=
            " and occurreddate "
                + (boundary.getAnalyticsPeriodBoundaryType().isStartBoundary() ? ">=" : "<")
                + " cast( '"
                + format.format(boundary.getBoundaryDate(reportingStartDate, reportingEndDate))
                + "' as date )";
      }

      sql += " limit 1)";
    }

    return sql;
  }

  private String toSqlCondition(Period period, TimeField timeField) {
    String timeCol = sqlBuilder.quoteAx(timeField.getEnrollmentColumnName());
    return "( "
        + timeCol
        + " >= '"
        + toMediumDate(period.getStartDate())
        + "' and "
        + timeCol
        + " < '"
        + toMediumDate(plusOneDay(period.getEndDate()))
        + "') ";
  }
}
