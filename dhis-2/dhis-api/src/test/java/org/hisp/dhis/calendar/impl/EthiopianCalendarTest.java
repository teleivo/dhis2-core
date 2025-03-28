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
package org.hisp.dhis.calendar.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.List;
import org.hisp.dhis.calendar.Calendar;
import org.hisp.dhis.calendar.DateTimeUnit;
import org.hisp.dhis.period.Cal;
import org.hisp.dhis.period.DailyPeriodType;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.period.WeeklyPeriodType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
class EthiopianCalendarTest {

  private Calendar calendar;

  @BeforeEach
  void init() {
    calendar = EthiopianCalendar.getInstance();
  }

  @Test
  void testIsoStartOfYear() {
    DateTimeUnit startOfYear = calendar.isoStartOfYear(2007);
    assertEquals(2014, startOfYear.getYear());
    assertEquals(9, startOfYear.getMonth());
    assertEquals(11, startOfYear.getDay());
  }

  @Test
  void testDaysInMonth13() {
    assertThrows(RuntimeException.class, () -> calendar.daysInMonth(2007, 13));
  }

  @Test
  void testDaysInYear() {
    int daysInYear = calendar.daysInYear(2006);
    assertEquals(12 * 30, daysInYear);
  }

  @Test
  void testGenerateDailyPeriods() {
    Date startDate = new Cal(1975, 1, 1, true).time();
    Date endDate = new Cal(2025, 1, 2, true).time();
    List<Period> days = new DailyPeriodType().generatePeriods(calendar, startDate, endDate);
    assertEquals(18001, days.size());
  }

  @Test
  void testGenerateQuarterlyPeriods() {
    Date startDate = new Cal(1975, 1, 1, true).time();
    Date endDate = new Cal(2025, 1, 2, true).time();
    List<Period> quarters = new QuarterlyPeriodType().generatePeriods(calendar, startDate, endDate);
    assertEquals(201, quarters.size());
  }

  @Test
  void testGenerateMonthlyPeriods() {
    Date startDate = new Cal(1975, 1, 1, true).time();
    Date endDate = new Cal(2025, 1, 2, true).time();
    List<Period> monthly = new MonthlyPeriodType().generatePeriods(calendar, startDate, endDate);
    assertEquals(601, monthly.size());
  }

  @Test
  void testGenerateWeeklyPeriods() {
    Date startDate = new Cal(1975, 1, 1, true).time();
    Date endDate = new Cal(2025, 1, 2, true).time();
    List<Period> weeks = new WeeklyPeriodType().generatePeriods(calendar, startDate, endDate);
    assertEquals(2610, weeks.size());
  }

  @Test
  void testPlusDays() {
    DateTimeUnit dateTimeUnit = new DateTimeUnit(2006, 1, 1);
    DateTimeUnit testDateTimeUnit = calendar.plusDays(dateTimeUnit, 43);
    assertEquals(2006, testDateTimeUnit.getYear());
    assertEquals(2, testDateTimeUnit.getMonth());
    assertEquals(14, testDateTimeUnit.getDay());
    testDateTimeUnit = calendar.plusDays(dateTimeUnit, 65);
    assertEquals(2006, testDateTimeUnit.getYear());
    assertEquals(3, testDateTimeUnit.getMonth());
    assertEquals(6, testDateTimeUnit.getDay());
    testDateTimeUnit = calendar.plusDays(dateTimeUnit, (12 * 30) + 5);
    assertEquals(2007, testDateTimeUnit.getYear());
    assertEquals(1, testDateTimeUnit.getMonth());
    assertEquals(6, testDateTimeUnit.getDay());
    dateTimeUnit = new DateTimeUnit(2006, 2, 29);
    testDateTimeUnit = calendar.plusDays(dateTimeUnit, 10);
    assertEquals(2006, testDateTimeUnit.getYear());
    assertEquals(3, testDateTimeUnit.getMonth());
    assertEquals(9, testDateTimeUnit.getDay());
    dateTimeUnit = new DateTimeUnit(2006, 3, 9);
    testDateTimeUnit = calendar.plusDays(dateTimeUnit, -1);
    assertEquals(2006, testDateTimeUnit.getYear());
    assertEquals(3, testDateTimeUnit.getMonth());
    assertEquals(8, testDateTimeUnit.getDay());
    dateTimeUnit = new DateTimeUnit(2006, 1, 1);
    testDateTimeUnit = calendar.plusDays(dateTimeUnit, -1);
    assertEquals(2005, testDateTimeUnit.getYear());
    assertEquals(12, testDateTimeUnit.getMonth());
    assertEquals(30, testDateTimeUnit.getDay());
  }

  @Test
  void testMinusDays() {
    DateTimeUnit dateTimeUnit = new DateTimeUnit(2007, 1, 1);
    DateTimeUnit testDateTimeUnit = calendar.minusDays(dateTimeUnit, 2);
    assertEquals(2006, testDateTimeUnit.getYear());
    assertEquals(12, testDateTimeUnit.getMonth());
    assertEquals(29, testDateTimeUnit.getDay());
  }
}
