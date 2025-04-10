/*
 * Copyright (c) 2004-2023, University of Oslo
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AnalyticsPagingCriteria}.
 *
 * @author maikel arabori
 */
class AnalyticsPagingCriteriaTest {
  @Test
  void testDefinePageSizeWithLimitWhenPagingIsFalse() {
    // Given
    int maxLimit = 50000;
    AnalyticsPagingCriteria analyticsPagingCriteria = new AnalyticsPagingCriteria();
    analyticsPagingCriteria.setPageSize(50);
    analyticsPagingCriteria.setPaging(false);

    // When
    analyticsPagingCriteria.definePageSize(maxLimit);

    // Then
    assertEquals(maxLimit, analyticsPagingCriteria.getPageSize());
  }

  @Test
  void testDefinePageSizeWithLimitWhenPagingIsTrue() {
    // Given
    int maxLimit = 50000;
    AnalyticsPagingCriteria analyticsPagingCriteria = new AnalyticsPagingCriteria();
    analyticsPagingCriteria.setPageSize(50);
    analyticsPagingCriteria.setPaging(true);

    // When
    analyticsPagingCriteria.definePageSize(maxLimit);

    // Then
    assertEquals(50, analyticsPagingCriteria.getPageSize());
  }

  @Test
  void testDefinePageSizeWithLimitGreaterThanMaxAllowedWhenPagingIsFalse() {
    // Given
    int maxLimit = 50000;
    AnalyticsPagingCriteria analyticsPagingCriteria = new AnalyticsPagingCriteria();
    analyticsPagingCriteria.setPageSize(60000);
    analyticsPagingCriteria.setPaging(false);

    // When
    analyticsPagingCriteria.definePageSize(maxLimit);

    // Then
    assertEquals(maxLimit, analyticsPagingCriteria.getPageSize());
  }

  @Test
  void testDefinePageSizeWithLimitGreaterThanMaxAllowedWhenPagingIsTrue() {
    // Given
    int maxLimit = 50000;
    AnalyticsPagingCriteria analyticsPagingCriteria = new AnalyticsPagingCriteria();
    analyticsPagingCriteria.setPageSize(60000);
    analyticsPagingCriteria.setPaging(true);

    // When
    analyticsPagingCriteria.definePageSize(maxLimit);

    // Then
    assertEquals(maxLimit, analyticsPagingCriteria.getPageSize());
  }

  @Test
  void testDefinePageSizeUnlimitedWhenPagingIsFalse() {
    // Given
    int unlimited = 0;
    AnalyticsPagingCriteria analyticsPagingCriteria = new AnalyticsPagingCriteria();
    analyticsPagingCriteria.setPageSize(50);
    analyticsPagingCriteria.setPaging(false);

    // When
    analyticsPagingCriteria.definePageSize(unlimited);

    // Then
    assertEquals(unlimited, analyticsPagingCriteria.getPageSize());
  }
}
