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
package org.hisp.dhis.common;

import static org.hisp.dhis.analytics.QueryKey.NV;
import static org.hisp.dhis.common.QueryOperator.EQ;
import static org.hisp.dhis.common.QueryOperator.EW;
import static org.hisp.dhis.common.QueryOperator.GE;
import static org.hisp.dhis.common.QueryOperator.GT;
import static org.hisp.dhis.common.QueryOperator.IEQ;
import static org.hisp.dhis.common.QueryOperator.ILIKE;
import static org.hisp.dhis.common.QueryOperator.IN;
import static org.hisp.dhis.common.QueryOperator.LE;
import static org.hisp.dhis.common.QueryOperator.LIKE;
import static org.hisp.dhis.common.QueryOperator.LT;
import static org.hisp.dhis.common.QueryOperator.NE;
import static org.hisp.dhis.common.QueryOperator.NEQ;
import static org.hisp.dhis.common.QueryOperator.NIEQ;
import static org.hisp.dhis.common.QueryOperator.NILIKE;
import static org.hisp.dhis.common.QueryOperator.NLIKE;
import static org.hisp.dhis.common.QueryOperator.NNULL;
import static org.hisp.dhis.common.QueryOperator.NULL;
import static org.hisp.dhis.common.QueryOperator.SW;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lars Helge Overland
 */
@Getter
@Setter
public class QueryFilter {
  public static final String OPTION_SEP = ";";

  public static final ImmutableMap<QueryOperator, Function<Boolean, String>> OPERATOR_MAP =
      ImmutableMap.<QueryOperator, Function<Boolean, String>>builder()
          .put(EQ, isValueNull -> isValueNull ? "is" : "=")
          .put(NE, isValueNull -> isValueNull ? "is not" : "!=")
          .put(NEQ, isValueNull -> isValueNull ? "is not" : "!=")
          .put(IEQ, isValueNull -> isValueNull ? "is" : "=")
          .put(NIEQ, isValueNull -> isValueNull ? "is not" : "!=")
          .put(GT, unused -> ">")
          .put(GE, unused -> ">=")
          .put(LT, unused -> "<")
          .put(LE, unused -> "<=")
          .put(ILIKE, unused -> "ilike")
          .put(NILIKE, unused -> "not ilike")
          .put(LIKE, unused -> "like")
          .put(SW, unused -> "like")
          .put(EW, unused -> "like")
          .put(NLIKE, unused -> "not like")
          .put(IN, unused -> "in")
          .put(NULL, unused -> NULL.getValue())
          .put(NNULL, unused -> NNULL.getValue())
          .build();

  protected QueryOperator operator;

  protected String filter;

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  public QueryFilter() {}

  public QueryFilter(QueryOperator operator) {
    this.operator = operator;
  }

  public QueryFilter(QueryOperator operator, String filter) {
    this.operator = operator;
    this.filter = filter;
  }

  // -------------------------------------------------------------------------
  // Logic
  // -------------------------------------------------------------------------

  public boolean isFilter() {
    return operator != null && filter != null && !filter.isEmpty();
  }

  public String getSqlOperator() {
    return getSqlOperator(false);
  }

  /**
   * Returns a SQL operator string.
   *
   * @param isOperatorSubstitutionAllowed whether the operator should be replaced to support null
   *     values.
   * @return a SQL operator string.
   */
  public String getSqlOperator(boolean isOperatorSubstitutionAllowed) {
    if (operator == null) {
      return null;
    }

    return safelyGetOperator(isOperatorSubstitutionAllowed);
  }

  private String safelyGetOperator(boolean isOperatorSubstitutionAllowed) {
    Function<Boolean, String> operatorFunction = OPERATOR_MAP.get(operator);

    if (operatorFunction != null) {
      return operatorFunction.apply(
          StringUtils.trimToEmpty(filter).equalsIgnoreCase(NV) && isOperatorSubstitutionAllowed);
    }

    return null;
  }

  /**
   * Do not use this for JDBC template queries! This builds the SQL adding user input into it. So
   * this opens up the possibility of SQL injection. SW and EW might also not properly escape like
   * wildcards '%' and '_' while these operators are actually implemented using SQL like.
   */
  public String getSqlFilter(final String encodedFilter) {
    return getSqlFilter(encodedFilter, false);
  }

  public String getSqlFilter(final String encodedFilter, boolean isNullValueSubstitutionAllowed) {
    if (operator == null || encodedFilter == null) {
      return null;
    }

    if (operator.isLike()) {
      return "'%" + encodedFilter.replace("_", "\\_").replace("%", "\\%") + "%'";
    } else if (operator.isEqualTo()) {
      if (encodedFilter.equals(NV) && isNullValueSubstitutionAllowed) {
        return "null";
      }
    } else if (IN == operator) {
      return getFilterItems(encodedFilter).stream()
          .map(this::quote)
          .collect(Collectors.joining(",", "(", ")"));
    } else if (SW == operator) {
      return "'" + encodedFilter + "%'";
    } else if (EW == operator) {
      return "'%" + encodedFilter + "'";
    }

    return "'" + encodedFilter + "'";
  }

  public String getSqlBindFilter() {
    if (operator.isLike()) {
      return "%" + this.filter + "%";
    } else if (operator.isEqualTo()) {
      if (this.filter.equals(NV)) {
        return "null";
      }
    } else if (SW == operator) {
      return this.filter + "%";
    } else if (EW == operator) {
      return "%" + this.filter;
    }

    return this.filter;
  }

  /**
   * Returns the query filter with pre-/suffixed (affixed) '%' wildcards if the operator is based on
   * SQL <a
   * href="https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-LIKE">like</a>
   * (see {@link #OPERATOR_MAP}). Filters for non-like based operators are returned as is.
   *
   * <p>Make sure to escape like wildcards in user input before calling this method!
   */
  @Nonnull
  public static String affixLikeWildcards(@Nonnull QueryOperator operator, @Nonnull String filter) {
    if (operator.isLike()) {
      return "%" + filter + "%";
    } else if (operator == SW) {
      return filter + "%";
    } else if (operator == EW) {
      return "%" + filter;
    }

    return filter;
  }

  public String getSqlFilter(
      final String encodedFilter,
      final ValueType valueType,
      boolean isNullValueSubstitutionAllowed) {
    final String sqlFilter = getSqlFilter(encodedFilter, isNullValueSubstitutionAllowed);

    // Force lowercase to compare ignoring case

    if (IEQ == operator || NIEQ == operator) {
      return valueType.isText() ? sqlFilter.toLowerCase() : sqlFilter;
    }

    return sqlFilter;
  }

  public String getSqlFilterColumn(final String column, final ValueType valueType) {
    // Force lowercase to compare ignoring case

    if (IEQ == operator || NIEQ == operator) {
      return valueType.isText() ? wrapLower(column) : column;
    }

    return column;
  }

  /**
   * Wraps the provided column name in Postgres 'lower' directive
   *
   * @param column a column name
   * @return a String
   */
  private String wrapLower(String column) {
    return "lower(" + column + ")";
  }

  protected String quote(String filterItem) {
    return "'" + filterItem + "'";
  }

  /**
   * Returns the items of the filter.
   *
   * @param encodedFilter the encoded filter.
   */
  public static List<String> getFilterItems(String encodedFilter) {
    return Lists.newArrayList(encodedFilter.split(OPTION_SEP));
  }

  /** Returns a string representation of the query operator and filter. */
  public String getFilterAsString() {
    return operator.getValue() + " " + filter;
  }

  // -------------------------------------------------------------------------
  // hashCode, equals and toString
  // -------------------------------------------------------------------------

  @Override
  public String toString() {
    return "[Operator: " + operator + ", filter: " + filter + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((filter == null) ? 0 : filter.hashCode());
    result = prime * result + ((operator == null) ? 0 : operator.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (object == null) {
      return false;
    }

    if (getClass() != object.getClass()) {
      return false;
    }

    QueryFilter other = (QueryFilter) object;

    if (filter == null) {
      if (other.filter != null) {
        return false;
      }
    } else if (!filter.equals(other.filter)) {
      return false;
    }

    if (operator == null) {
      return other.operator == null;
    } else {
      return operator.equals(other.operator);
    }
  }
}
