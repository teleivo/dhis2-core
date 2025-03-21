/*
 * Copyright (c) 2004-2022, University of Oslo
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
package org.hisp.dhis.webapi.controller.tracker;

import static org.hisp.dhis.common.DimensionalObject.DIMENSION_NAME_SEP;
import static org.hisp.dhis.common.OrganisationUnitSelectionMode.ACCESSIBLE;
import static org.hisp.dhis.common.OrganisationUnitSelectionMode.ALL;
import static org.hisp.dhis.common.OrganisationUnitSelectionMode.CAPTURE;
import static org.hisp.dhis.common.OrganisationUnitSelectionMode.CHILDREN;
import static org.hisp.dhis.common.OrganisationUnitSelectionMode.DESCENDANTS;
import static org.hisp.dhis.common.OrganisationUnitSelectionMode.SELECTED;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.common.QueryFilter;
import org.hisp.dhis.common.QueryOperator;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.common.collection.CollectionUtils;
import org.hisp.dhis.commons.util.TextUtils;
import org.hisp.dhis.feedback.BadRequestException;
import org.hisp.dhis.util.ObjectUtils;
import org.hisp.dhis.webapi.controller.event.webrequest.OrderCriteria;

/**
 * RequestParamUtils are functions used to parse and transform tracker request parameters. This
 * class is intended to only house functions without any dependencies on services or components.
 */
public class RequestParamsValidator {
  private RequestParamsValidator() {
    throw new IllegalStateException("Utility class");
  }

  private static final String INVALID_FILTER = "Query item or filter is invalid: ";

  private static final char COMMA_SEPARATOR = ',';

  private static final char ESCAPE = '/';

  private static final String SUPPORTED_CHANGELOG_FILTER_OPERATOR = "eq";

  /**
   * Negative lookahead to avoid wrong split of comma-separated list of filters when one or more
   * filter value contain comma. It skips comma escaped by slash
   */
  private static final Pattern FILTER_LIST_SPLIT =
      Pattern.compile("(?<!" + ESCAPE + ")" + COMMA_SEPARATOR);

  /**
   * Negative lookahead to avoid wrong split when filter value contains colon. It skips colon
   * escaped by slash
   */
  public static final Pattern FILTER_ITEM_SPLIT =
      Pattern.compile("(?<!" + ESCAPE + ")" + DIMENSION_NAME_SEP);

  private static final String COMMA_STRING = Character.toString(COMMA_SEPARATOR);

  private static final String ESCAPE_COMMA = ESCAPE + COMMA_STRING;

  private static final String ESCAPE_COLON = ESCAPE + DIMENSION_NAME_SEP;

  /**
   * Helps us transition request parameters that contained semicolon separated UIDs (deprecated) to
   * comma separated UIDs in a backwards compatible way.
   *
   * @param deprecatedParamName request parameter name of deprecated semi-colon separated parameter
   * @param deprecatedParamUids semicolon separated uids
   * @param newParamName new request parameter replacing deprecated request parameter
   * @param newParamUids new request parameter uids
   * @return uids from the request parameter containing uids
   * @throws BadRequestException when both deprecated and new request parameter contain uids
   */
  public static Set<UID> validateDeprecatedUidsParameter(
      String deprecatedParamName,
      String deprecatedParamUids,
      String newParamName,
      Set<UID> newParamUids)
      throws BadRequestException {
    Set<String> deprecatedParamParsedUids = parseUids(deprecatedParamUids);
    if (!deprecatedParamParsedUids.isEmpty() && !newParamUids.isEmpty()) {
      throw new BadRequestException(
          String.format(
              "Only one parameter of '%s' (deprecated; semicolon separated UIDs) and '%s' (comma"
                  + " separated UIDs) must be specified. Prefer '%s' as '%s' will be removed.",
              deprecatedParamName, newParamName, newParamName, deprecatedParamName));
    }

    return !deprecatedParamParsedUids.isEmpty()
        ? deprecatedParamParsedUids.stream().map(UID::of).collect(Collectors.toSet())
        : newParamUids;
  }

  /**
   * Helps us transition request parameters from a deprecated to a new one.
   *
   * @param deprecatedParamName request parameter name of deprecated parameter
   * @param deprecatedParam value of deprecated request parameter
   * @param newParamName new request parameter replacing deprecated request parameter
   * @param newParam value of the request parameter
   * @return value of the one request parameter that is non-null
   * @throws BadRequestException when both deprecated and new request parameter are non-null
   */
  public static <T> T validateDeprecatedParameter(
      String deprecatedParamName, T deprecatedParam, String newParamName, T newParam)
      throws BadRequestException {
    if (newParam != null && deprecatedParam != null) {
      throw new BadRequestException(
          String.format(
              "Only one parameter of '%s' and '%s' must be specified. Prefer '%s' as '%s' will be"
                  + " removed.",
              deprecatedParamName, newParamName, newParamName, deprecatedParamName));
    }

    return newParam != null ? newParam : deprecatedParam;
  }

  /**
   * Helps us transition mandatory request parameters from a deprecated to a new one. At least one
   * parameter must be non-empty as the deprecated one was mandatory.
   *
   * @param deprecatedParamName request parameter name of deprecated parameter
   * @param deprecatedParam value of deprecated request parameter
   * @param newParamName new request parameter replacing deprecated request parameter
   * @param newParam value of the request parameter
   * @return value of the one request parameter that is non-empty
   * @throws BadRequestException when both deprecated and new request parameter are non-null
   * @throws BadRequestException when both deprecated and new request parameter are null
   */
  public static UID validateMandatoryDeprecatedUidParameter(
      String deprecatedParamName, UID deprecatedParam, String newParamName, UID newParam)
      throws BadRequestException {
    UID uid =
        validateDeprecatedParameter(deprecatedParamName, deprecatedParam, newParamName, newParam);

    if (uid == null) {
      throw new BadRequestException(
          String.format("Required request parameter '%s' is not present", newParamName));
    }

    return uid;
  }

  /**
   * Parse semicolon separated string of UIDs.
   *
   * @param input string to parse
   * @return set of uids
   */
  private static Set<String> parseUids(String input) {
    return parseUidString(input).collect(Collectors.toSet());
  }

  private static Stream<String> parseUidString(String input) {
    return CollectionUtils.emptyIfNull(TextUtils.splitToSet(input, TextUtils.SEMICOLON)).stream();
  }

  /**
   * Validate the {@code order} request parameter in tracker exporters. Allowed order values are
   * {@code supportedFieldNames} and UIDs which represent {@code uidMeaning}. Every field name or
   * UID can be specified at most once.
   */
  public static void validateOrderParams(
      List<OrderCriteria> order, Set<String> supportedFieldNames, String uidMeaning)
      throws BadRequestException {
    if (order == null || order.isEmpty()) {
      return;
    }

    Set<String> invalidOrderComponents =
        order.stream().map(OrderCriteria::getField).collect(Collectors.toSet());
    invalidOrderComponents.removeAll(supportedFieldNames);
    Set<String> uids =
        invalidOrderComponents.stream()
            .filter(CodeGenerator::isValidUid)
            .collect(Collectors.toSet());
    invalidOrderComponents.removeAll(uids);

    String errorSuffix =
        String.format(
            "Supported are %s UIDs and fields '%s'. All of which can at most be specified once.",
            uidMeaning, String.join(", ", supportedFieldNames.stream().sorted().toList()));
    if (!invalidOrderComponents.isEmpty()) {
      throw new BadRequestException(
          String.format(
              "order parameter is invalid. Cannot order by '%s'. %s",
              String.join(", ", invalidOrderComponents), errorSuffix));
    }

    validateOrderParamsContainNoDuplicates(order, errorSuffix);
  }

  private static void validateOrderParamsContainNoDuplicates(
      List<OrderCriteria> order, String errorSuffix) throws BadRequestException {
    Set<String> duplicateOrderComponents =
        order.stream()
            .map(OrderCriteria::getField)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet()
            .stream()
            .filter(e -> e.getValue() > 1)
            .map(Entry::getKey)
            .collect(Collectors.toSet());

    if (!duplicateOrderComponents.isEmpty()) {
      throw new BadRequestException(
          String.format(
              "order parameter is invalid. '%s' are repeated. %s",
              String.join(", ", duplicateOrderComponents), errorSuffix));
    }
  }

  /**
   * Validate the {@code order} request parameter in tracker exporters. Allowed order values are
   * {@code supportedFieldNames}. Every field name can be specified at most once. If the endpoint
   * supports field names and UIDs use {@link #validateOrderParams(List, Set, String)}.
   */
  public static void validateOrderParams(List<OrderCriteria> order, Set<String> supportedFieldNames)
      throws BadRequestException {
    if (order == null || order.isEmpty()) {
      return;
    }

    Set<String> invalidOrderComponents =
        order.stream().map(OrderCriteria::getField).collect(Collectors.toSet());
    invalidOrderComponents.removeAll(supportedFieldNames);

    String errorSuffix =
        String.format(
            "Supported fields are '%s'. All of which can at most be specified once.",
            String.join(", ", supportedFieldNames.stream().sorted().toList()));
    if (!invalidOrderComponents.isEmpty()) {
      throw new BadRequestException(
          String.format(
              "order parameter is invalid. Cannot order by '%s'. %s",
              String.join(", ", invalidOrderComponents), errorSuffix));
    }

    validateOrderParamsContainNoDuplicates(order, errorSuffix);
  }

  /**
   * Validate the {@code filter} request parameter in change log tracker exporters. Allowed filter
   * values are {@code supportedFieldNames}. Only one field name at a time can be specified. If the
   * endpoint supports UIDs use {@link #parseFilters(String)}.
   */
  public static void validateFilter(String filter, Set<Pair<String, Class<?>>> supportedFields)
      throws BadRequestException {
    if (filter == null) {
      return;
    }

    String[] split = filter.split(":");
    Set<String> supportedFieldNames =
        supportedFields.stream().map(Pair::getKey).collect(Collectors.toSet());

    if (split.length != 3) {
      throw new BadRequestException(
          String.format(
              "Invalid filter => %s. Expected format is [field]:eq:[value]. Supported fields are"
                  + " '%s'. Only one of them can be specified at a time",
              filter, String.join(", ", supportedFieldNames)));
    }

    if (!supportedFieldNames.contains(split[0])) {
      throw new BadRequestException(
          String.format(
              "Invalid filter field. Supported fields are '%s'. Only one of them can be specified"
                  + " at a time",
              String.join(", ", supportedFieldNames)));
    }

    if (!SUPPORTED_CHANGELOG_FILTER_OPERATOR.equalsIgnoreCase(split[1])) {
      throw new BadRequestException(
          String.format(
              "Invalid filter operator. The only supported operator is '%s'.",
              SUPPORTED_CHANGELOG_FILTER_OPERATOR));
    }

    for (Pair<String, Class<?>> filterField : supportedFields) {
      if (filterField.getKey().equalsIgnoreCase(split[0])
          && filterField.getValue() == UID.class
          && !CodeGenerator.isValidUid(split[2])) {
        throw new BadRequestException(
            String.format(
                "Incorrect filter value provided as UID: %s. UID must be an alphanumeric string of"
                    + " 11 characters starting with a letter.",
                split[2]));
      }
    }
  }

  /**
   * Parse given {@code input} string representing a filter for an object referenced by a UID like a
   * tracked entity attribute. Refer to {@link #parseSanitizedFilters(Map, String)}} for details on
   * the expected input format.
   *
   * @return filters by UIDs
   */
  public static Map<UID, List<QueryFilter>> parseFilters(String input) throws BadRequestException {
    Map<UID, List<QueryFilter>> result = new HashMap<>();
    if (StringUtils.isBlank(input)) {
      return result;
    }

    for (String uidOperatorValue : filterList(input)) {
      parseSanitizedFilters(result, uidOperatorValue);
    }
    return result;
  }

  /**
   * Accumulate {@link QueryFilter}s per TEA UID by parsing given input string of format
   * {uid}[:{operator}:{value}]. Only the TEA UID is mandatory. Multiple operator:value pairs are
   * allowed. A {@link QueryFilter} for each operator:value pair is added to the corresponding TEA
   * UID.
   *
   * @throws BadRequestException filter is neither multiple nor single operator:value format
   */
  private static void parseSanitizedFilters(Map<UID, List<QueryFilter>> result, String input)
      throws BadRequestException {
    int uidIndex = input.indexOf(DIMENSION_NAME_SEP) + 1;

    if (uidIndex == 0 || input.length() == uidIndex) {
      UID uid = UID.of(input.replace(DIMENSION_NAME_SEP, ""));
      result.putIfAbsent(uid, new ArrayList<>());
      return;
    }

    UID uid = UID.of(input.substring(0, uidIndex - 1));
    result.putIfAbsent(uid, new ArrayList<>());

    String[] filters = FILTER_ITEM_SPLIT.split(input.substring(uidIndex));
    validateFilterLength(filters, result, uid, input);
  }

  private static void validateFilterLength(
      String[] filters, Map<UID, List<QueryFilter>> result, UID uid, String input)
      throws BadRequestException {
    switch (filters.length) {
      case 1 -> addQueryFilter(result, uid, filters[0], null, input);
      case 2 -> handleOperators(filters, result, uid, input);
      case 3 -> handleMixedOperators(filters, result, uid, input);
      case 4 -> handleMultipleBinaryOperators(filters, result, uid, input);
      default -> throw new BadRequestException(INVALID_FILTER + input);
    }
  }

  private static void addQueryFilter(
      Map<UID, List<QueryFilter>> result, UID uid, String operator, String value, String input)
      throws BadRequestException {
    result.get(uid).add(operatorValueQueryFilter(operator, value, input));
  }

  private static void handleOperators(
      String[] filters, Map<UID, List<QueryFilter>> result, UID uid, String input)
      throws BadRequestException {
    QueryOperator firstOperator =
        findQueryOperatorFromFilter(filters[0])
            .orElseThrow(
                () ->
                    new BadRequestException(
                        String.format("'%s' is not a valid operator: %s", filters[0], input)));

    if (!firstOperator.isUnary()) {
      addQueryFilter(result, uid, filters[0], filters[1], input);
      return;
    }

    QueryOperator secondOperator =
        findQueryOperatorFromFilter(filters[1])
            .orElseThrow(
                () ->
                    new BadRequestException(
                        String.format(
                            "Operator '%s' in filter can't be used with a value: %s",
                            filters[0], input)));

    if (secondOperator.isUnary()) {
      addQueryFilter(result, uid, filters[0], null, input);
      addQueryFilter(result, uid, filters[1], null, input);
    }
  }

  private static void handleMixedOperators(
      String[] filters, Map<UID, List<QueryFilter>> result, UID uid, String input)
      throws BadRequestException {
    Optional<QueryOperator> firstOperator = findQueryOperatorFromFilter(filters[0]);
    if (firstOperator.map(QueryOperator::isUnary).orElse(false)) {
      addQueryFilter(result, uid, filters[0], null, input);
      addQueryFilter(result, uid, filters[1], filters[2], input);
      return;
    }

    Optional<QueryOperator> thirdOperator = findQueryOperatorFromFilter(filters[2]);
    if (thirdOperator.map(QueryOperator::isUnary).orElse(false)) {
      addQueryFilter(result, uid, filters[0], filters[1], input);
      addQueryFilter(result, uid, filters[2], null, input);
      return;
    }

    throw new BadRequestException(INVALID_FILTER + input);
  }

  private static void handleMultipleBinaryOperators(
      String[] filters, Map<UID, List<QueryFilter>> result, UID uid, String input)
      throws BadRequestException {

    List<String> unaryOperators = getUnaryOperatorsInFilter(filters);
    switch (unaryOperators.size()) {
      case 0 -> {
        for (int i = 0; i < filters.length; i += 2) {
          addQueryFilter(result, uid, filters[i], filters[i + 1], input);
        }
      }
      case 1 ->
          throw new BadRequestException(
              String.format(
                  "Operator '%s' in filter can't be used with a value: %s",
                  unaryOperators.get(0), input));
      default ->
          throw new BadRequestException(
              String.format("A maximum of two operators can be used in a filter: %s", input));
    }
  }

  private static Optional<QueryOperator> findQueryOperatorFromFilter(String filter) {
    return Arrays.stream(QueryOperator.values())
        .filter(qo -> qo.name().equalsIgnoreCase(filter.replace("!", "n")))
        .findFirst();
  }

  private static List<String> getUnaryOperatorsInFilter(String[] filters) {
    Set<String> unaryOperators =
        Arrays.stream(QueryOperator.values())
            .filter(QueryOperator::isUnary)
            .map(qo -> qo.name().toLowerCase())
            .collect(Collectors.toSet());

    return Arrays.stream(filters)
        .map(f -> f.toLowerCase().replace("!", "n"))
        .filter(unaryOperators::contains)
        .toList();
  }

  public static OrganisationUnitSelectionMode validateOrgUnitModeForTrackedEntities(
      Set<UID> orgUnits, OrganisationUnitSelectionMode orgUnitMode, Set<UID> trackedEntities)
      throws BadRequestException {

    orgUnitMode = validateOrgUnitMode(orgUnitMode, orgUnits);
    validateOrgUnitOrTrackedEntityIsPresent(orgUnitMode, orgUnits, trackedEntities);

    return orgUnitMode;
  }

  public static OrganisationUnitSelectionMode validateOrgUnitModeForEnrollmentsAndEvents(
      Set<UID> orgUnits, OrganisationUnitSelectionMode orgUnitMode) throws BadRequestException {

    orgUnitMode = validateOrgUnitMode(orgUnitMode, orgUnits);
    validateOrgUnitIsPresent(orgUnitMode, orgUnits);

    return orgUnitMode;
  }

  /**
   * Validates that no org unit is present if the orgUnitMode is ACCESSIBLE or CAPTURE. If it is, an
   * exception will be thrown. If the org unit mode is not defined, SELECTED will be used by default
   * if an org unit is present. Otherwise, ACCESSIBLE will be the default.
   *
   * @param orgUnits list of org units to be validated
   * @return a valid org unit mode
   * @throws BadRequestException if a wrong combination of org unit and org unit mode supplied
   */
  private static OrganisationUnitSelectionMode validateOrgUnitMode(
      OrganisationUnitSelectionMode orgUnitMode, Set<UID> orgUnits) throws BadRequestException {
    if (orgUnitMode == null) {
      return orgUnits.isEmpty() ? ACCESSIBLE : SELECTED;
    }

    if (orgUnitModeDoesNotRequireOrgUnit(orgUnitMode) && !orgUnits.isEmpty()) {
      throw new BadRequestException(
          String.format(
              "orgUnitMode %s cannot be used with orgUnits. Please remove the orgUnit parameter and"
                  + " try again.",
              orgUnitMode));
    }

    return orgUnitMode;
  }

  private static void validateOrgUnitOrTrackedEntityIsPresent(
      OrganisationUnitSelectionMode orgUnitMode, Set<UID> orgUnits, Set<UID> trackedEntities)
      throws BadRequestException {
    if (orgUnitModeRequiresOrgUnit(orgUnitMode)
        && orgUnits.isEmpty()
        && trackedEntities.isEmpty()) {
      throw new BadRequestException(
          String.format(
              "At least one org unit or tracked entity is required for orgUnitMode: %s. Please add"
                  + " one of the two or use a different orgUnitMode.",
              orgUnitMode));
    }
  }

  private static void validateOrgUnitIsPresent(
      OrganisationUnitSelectionMode orgUnitMode, Set<UID> orgUnits) throws BadRequestException {
    if (orgUnitModeRequiresOrgUnit(orgUnitMode) && orgUnits.isEmpty()) {
      throw new BadRequestException(
          String.format(
              "At least one org unit is required for orgUnitMode: %s. Please add one org unit or"
                  + " use a different orgUnitMode.",
              orgUnitMode));
    }
  }

  private static boolean orgUnitModeRequiresOrgUnit(OrganisationUnitSelectionMode orgUnitMode) {
    return orgUnitMode == CHILDREN || orgUnitMode == SELECTED || orgUnitMode == DESCENDANTS;
  }

  private static boolean orgUnitModeDoesNotRequireOrgUnit(
      OrganisationUnitSelectionMode orgUnitMode) {
    return orgUnitMode == ACCESSIBLE || orgUnitMode == CAPTURE || orgUnitMode == ALL;
  }

  public static void validatePaginationParameters(PageRequestParams params)
      throws BadRequestException {
    if (!params.isPaging()
        && (ObjectUtils.firstNonNull(params.getPage(), params.getPageSize()) != null
            || params.isTotalPages())) {
      throw new BadRequestException(
          "Paging cannot be disabled with paging=false while also requesting a paginated"
              + " response with page, pageSize and/or totalPages=true");
    }
  }

  public static void validateUnsupportedParameter(
      HttpServletRequest request, String dimension, String message) throws BadRequestException {
    if (StringUtils.isNotEmpty(request.getParameter(dimension))) {
      throw new BadRequestException(message);
    }
  }

  private static QueryFilter operatorValueQueryFilter(String operator, String value, String filter)
      throws BadRequestException {
    if (StringUtils.isEmpty(operator)) {
      throw new BadRequestException(INVALID_FILTER + filter);
    }

    QueryOperator queryOperator;
    try {
      queryOperator = QueryOperator.fromString(operator);
    } catch (IllegalArgumentException exception) {
      throw new BadRequestException(INVALID_FILTER + filter);
    }

    if (queryOperator == null) {
      throw new BadRequestException(INVALID_FILTER + filter);
    }

    if (queryOperator.isUnary()) {
      if (!StringUtils.isEmpty(value)) {
        throw new BadRequestException(
            String.format(
                "Operator %s in filter can't be used with a value: %s",
                queryOperator.name(), filter));
      }
      return new QueryFilter(queryOperator);
    }

    if (StringUtils.isEmpty(value)) {
      throw new BadRequestException("Operator in filter must be be used with a value: " + filter);
    }

    return new QueryFilter(queryOperator, escapedFilterValue(value));
  }

  /** Replace escaped comma or colon */
  private static String escapedFilterValue(String value) {
    return value.replace(ESCAPE_COMMA, COMMA_STRING).replace(ESCAPE_COLON, DIMENSION_NAME_SEP);
  }

  /**
   * Given an attribute filter list, first, it removes the escape chars in order to be able to split
   * by comma and collect the filter list. Then, it recreates the original filters by restoring the
   * escapes chars if any.
   *
   * @return a filter list split by comma
   */
  private static List<String> filterList(String filterItem) {
    Map<Integer, Boolean> escapesToRestore = new HashMap<>();

    StringBuilder filterListToEscape = new StringBuilder(filterItem);

    List<String> filters = new LinkedList<>();

    for (int i = 0; i < filterListToEscape.length() - 1; i++) {
      if (filterListToEscape.charAt(i) == ESCAPE && filterListToEscape.charAt(i + 1) == ESCAPE) {
        filterListToEscape.delete(i, i + 2);
        escapesToRestore.put(i, false);
      }
    }

    String[] escapedFilterList = FILTER_LIST_SPLIT.split(filterListToEscape);

    int beginning = 0;

    for (String escapedFilter : escapedFilterList) {
      filters.add(
          restoreEscape(
              escapesToRestore,
              new StringBuilder(escapedFilter),
              beginning,
              escapedFilter.length()));
      beginning += escapedFilter.length() + 1;
    }

    return filters;
  }

  /**
   * Restores the escape char in a filter based on the position in the original filter. It uses a
   * pad as in a filter there can be more than one escape char removed.
   *
   * @return a filter with restored escape chars
   */
  private static String restoreEscape(
      Map<Integer, Boolean> escapesToRestore, StringBuilder filter, int beginning, int end) {
    int pad = 0;
    for (Map.Entry<Integer, Boolean> slashPositionInFilter : escapesToRestore.entrySet()) {
      if (!slashPositionInFilter.getValue()
          && slashPositionInFilter.getKey() <= (beginning + end)) {
        filter.insert(slashPositionInFilter.getKey() - beginning + pad++, ESCAPE);
        escapesToRestore.put(slashPositionInFilter.getKey(), true);
      }
    }

    return filter.toString();
  }
}
