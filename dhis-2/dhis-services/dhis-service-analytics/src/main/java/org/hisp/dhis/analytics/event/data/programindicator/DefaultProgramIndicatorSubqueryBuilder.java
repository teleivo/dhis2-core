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
package org.hisp.dhis.analytics.event.data.programindicator;

import static org.hisp.dhis.analytics.DataType.BOOLEAN;
import static org.hisp.dhis.analytics.DataType.NUMERIC;

import com.google.common.base.Strings;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.hisp.dhis.analytics.AggregationType;
import org.hisp.dhis.analytics.AnalyticsTableType;
import org.hisp.dhis.analytics.DataType;
import org.hisp.dhis.analytics.common.CteContext;
import org.hisp.dhis.analytics.common.ProgramIndicatorSubqueryBuilder;
import org.hisp.dhis.analytics.table.model.AnalyticsTable;
import org.hisp.dhis.commons.util.TextUtils;
import org.hisp.dhis.program.AnalyticsType;
import org.hisp.dhis.program.ProgramIndicator;
import org.hisp.dhis.program.ProgramIndicatorService;
import org.hisp.dhis.relationship.RelationshipType;
import org.hisp.dhis.setting.SystemSettingsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultProgramIndicatorSubqueryBuilder implements ProgramIndicatorSubqueryBuilder {
  private static final Map<AnalyticsType, AnalyticsTableType> ANALYTICS_TYPE_MAP =
      Map.of(
          AnalyticsType.EVENT, AnalyticsTableType.EVENT,
          AnalyticsType.ENROLLMENT, AnalyticsTableType.ENROLLMENT);

  private static final String SUBQUERY_TABLE_ALIAS = "subax";

  private final ProgramIndicatorService programIndicatorService;
  private final SystemSettingsService settingsService;

  @Override
  public String getAggregateClauseForProgramIndicator(
      ProgramIndicator pi, AnalyticsType outerSqlEntity, Date earliestStartDate, Date latestDate) {
    return getAggregateClauseForPIandRelationshipType(
        pi, null, outerSqlEntity, earliestStartDate, latestDate);
  }

  @Override
  public String getAggregateClauseForProgramIndicator(
      ProgramIndicator programIndicator,
      RelationshipType relationshipType,
      AnalyticsType outerSqlEntity,
      Date earliestStartDate,
      Date latestDate) {
    return getAggregateClauseForPIandRelationshipType(
        programIndicator, relationshipType, outerSqlEntity, earliestStartDate, latestDate);
  }

  @Override
  public void addCte(
      ProgramIndicator programIndicator,
      AnalyticsType outerSqlEntity,
      Date earliestStartDate,
      Date latestDate,
      CteContext cteContext) {
    addCte(programIndicator, null, outerSqlEntity, earliestStartDate, latestDate, cteContext);
  }

  @Override
  public void addCte(
      ProgramIndicator programIndicator,
      RelationshipType relationshipType,
      AnalyticsType outerSqlEntity,
      Date earliestStartDate,
      Date latestDate,
      CteContext cteContext) {

    // Define aggregation function
    String function =
        TextUtils.emptyIfEqual(
            programIndicator.getAggregationTypeFallback().getValue(),
            AggregationType.CUSTOM.getValue());

    String filter = "";
    if (programIndicator.hasFilter()) {
      String piResolvedSqlFilter =
          getProgramIndicatorSql(
              programIndicator.getFilter(),
              NUMERIC,
              programIndicator,
              earliestStartDate,
              latestDate);
      filter = "where " + piResolvedSqlFilter;
    }

    String piResolvedSql =
        getProgramIndicatorSql(
            programIndicator.getExpression(),
            NUMERIC,
            programIndicator,
            earliestStartDate,
            latestDate);
    String cteSql =
        "select enrollment, %s(%s) as value from %s as subax %s group by enrollment"
            .formatted(function, piResolvedSql, getTableName(programIndicator), filter);

    // Register the CTE and its column mapping
    cteContext.addProgramIndicatorCte(programIndicator, cteSql, requireCoalesce(function));
  }

  /**
   * Determine if the aggregation function requires a COALESCE function to handle NULL values.
   *
   * @param function the aggregation function
   * @return true if the function requires a COALESCE function, false otherwise
   */
  private boolean requireCoalesce(String function) {
    return switch (function.toLowerCase()) {
      // removed "avg" from list because it seems that it does not require COALESCE
      // even though it is an aggregation function
      case "count", "sum", "min", "max" -> true;
      default -> false;
    };
  }

  private String getTableName(ProgramIndicator programIndicator) {
    return AnalyticsTable.getTableName(
        ANALYTICS_TYPE_MAP.get(programIndicator.getAnalyticsType()), programIndicator.getProgram());
  }

  /**
   * Generate a subquery based on the result of a Program Indicator and an (optional) Relationship
   * Type
   *
   * @param programIndicator the {@link ProgramIndicator}.
   * @param relationshipType the optional {@link RelationshipType} object
   * @param outerSqlEntity the {@link AnalyticsType} object representing the outer SQL context.
   * @param earliestStartDate reporting start date.
   * @param latestDate reporting end date.
   * @return a string containing a program indicator sub query.
   */
  private String getAggregateClauseForPIandRelationshipType(
      ProgramIndicator programIndicator,
      RelationshipType relationshipType,
      AnalyticsType outerSqlEntity,
      Date earliestStartDate,
      Date latestDate) {
    // Define aggregation function (avg, sum, ...) //
    String function =
        TextUtils.emptyIfEqual(
            programIndicator.getAggregationTypeFallback().getValue(),
            AggregationType.CUSTOM.getValue());

    // Get sql construct from Program indicator expression //
    String aggregateSql =
        getProgramIndicatorSql(
            programIndicator.getExpression(),
            NUMERIC,
            programIndicator,
            earliestStartDate,
            latestDate);

    // closes the function parenthesis ( avg( ... ) )
    aggregateSql += ")";

    // Determine Table name from FROM clause
    aggregateSql += getFrom(programIndicator);

    // Determine JOIN
    String where = getWhere(outerSqlEntity, programIndicator, relationshipType);

    aggregateSql += where;

    // Get WHERE condition from Program indicator filter
    if (!Strings.isNullOrEmpty(programIndicator.getFilter())) {
      aggregateSql +=
          (where.isBlank() ? " WHERE " : " AND ")
              + "("
              + getProgramIndicatorSql(
                  programIndicator.getFilter(),
                  BOOLEAN,
                  programIndicator,
                  earliestStartDate,
                  latestDate)
              + ")";
    }

    return "(SELECT " + function + " (" + aggregateSql + ")";
  }

  private String getFrom(ProgramIndicator pi) {
    AnalyticsTableType tableType = ANALYTICS_TYPE_MAP.get(pi.getAnalyticsType());
    return " FROM "
        + AnalyticsTable.getTableName(tableType, pi.getProgram())
        + " as "
        + SUBQUERY_TABLE_ALIAS;
  }

  /**
   * Determine the join after the WHERE condition. The rules are:
   *
   * <p>1) outer = event | inner = enrollment -> en = ax.enrollment (enrollment is the enrollment
   * linked to the inline event) 2) outer = enrollment | inner = event -> en = ax.enrollment 3)
   * outer = event | inner = event -> ev = ax.event (inner operate on same event as outer) 4) outer
   * = enrollment | inner = enrollment -> en = ax.enrollment (enrollment operates on the same
   * enrollment as outer) 5) if RelationshipType, call the RelationshipTypeJoinGenerator
   *
   * @param outerSqlEntity the outer {@link AnalyticsType}.
   * @param programIndicator the {@link ProgramIndicator}.
   * @param relationshipType the optional {@link RelationshipType}.
   * @return a SQL where clause.
   */
  private String getWhere(
      AnalyticsType outerSqlEntity,
      ProgramIndicator programIndicator,
      RelationshipType relationshipType) {
    String condition = "";
    if (relationshipType != null) {
      condition =
          RelationshipTypeJoinGenerator.generate(
              SUBQUERY_TABLE_ALIAS, relationshipType, programIndicator.getAnalyticsType());
    } else {
      if (AnalyticsType.ENROLLMENT == outerSqlEntity) {
        condition = useExperimentalAnalyticsQueryEngine() ? "" : "enrollment = ax.enrollment";
      } else {
        if (AnalyticsType.EVENT == programIndicator.getAnalyticsType()) {
          condition = useExperimentalAnalyticsQueryEngine() ? "" : "event = ax.event";
        }
      }
    }

    return !condition.isEmpty() ? " WHERE " + condition : "";
  }

  /**
   * Returns a program indicator SQL query.
   *
   * @param expression the program indicator expression.
   * @param dataType the {@link DataType}.
   * @param programIndicator the {@link ProgramIndicator}.
   * @param earliestStartDate the earliest start date.
   * @param latestDate the latest start date.
   * @return a program indicator SQL query.
   */
  private String getProgramIndicatorSql(
      String expression,
      DataType dataType,
      ProgramIndicator programIndicator,
      Date earliestStartDate,
      Date latestDate) {
    return this.programIndicatorService.getAnalyticsSql(
        expression,
        dataType,
        programIndicator,
        earliestStartDate,
        latestDate,
        SUBQUERY_TABLE_ALIAS);
  }

  protected boolean useExperimentalAnalyticsQueryEngine() {
    return this.settingsService.getCurrentSettings().getUseExperimentalAnalyticsQueryEngine();
  }
}
