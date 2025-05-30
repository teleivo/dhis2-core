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
package org.hisp.dhis.program;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hisp.dhis.dataentryform.DataEntryForm;
import org.hisp.dhis.dataentryform.DataEntryFormService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.test.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Chau Thu Tran
 */
@Transactional
class ProgramStoreTest extends PostgresIntegrationTestBase {

  @Autowired private ProgramStore programStore;

  @Autowired private OrganisationUnitService organisationUnitService;

  @Autowired private DataEntryFormService dataEntryFormService;

  private OrganisationUnit organisationUnitA;

  private OrganisationUnit organisationUnitB;

  private Program programA;

  private Program programB;

  private Program programC;

  @BeforeEach
  void setUp() {
    organisationUnitA = createOrganisationUnit('A');
    organisationUnitService.addOrganisationUnit(organisationUnitA);
    organisationUnitB = createOrganisationUnit('B');
    organisationUnitService.addOrganisationUnit(organisationUnitB);
    programA = createProgram('A', new HashSet<>(), organisationUnitA);
    programA.setUid("UID-A");
    programB = createProgram('B', new HashSet<>(), organisationUnitA);
    programB.setUid("UID-B");
    programC = createProgram('C', new HashSet<>(), organisationUnitB);
    programC.setUid("UID-C");
  }

  @Test
  void testGetProgramsByType() {
    programStore.save(programA);
    programStore.save(programB);
    programC.setProgramType(ProgramType.WITHOUT_REGISTRATION);
    programStore.save(programC);
    List<Program> programs = programStore.getByType(ProgramType.WITH_REGISTRATION);
    assertTrue(equals(programs, programA, programB));
    programs = programStore.getByType(ProgramType.WITHOUT_REGISTRATION);
    assertTrue(equals(programs, programC));
  }

  @Test
  void testGetProgramsByDataEntryForm() {
    DataEntryForm formX = createDataEntryForm('X');
    DataEntryForm formY = createDataEntryForm('Y');
    dataEntryFormService.addDataEntryForm(formX);
    dataEntryFormService.addDataEntryForm(formY);
    programA.setDataEntryForm(formX);
    programB.setDataEntryForm(formX);
    programStore.save(programA);
    programStore.save(programB);
    programStore.save(programC);
    List<Program> withFormX = programStore.getByDataEntryForm(formX);
    assertEquals(2, withFormX.size());
    assertFalse(withFormX.contains(programC));
    programC.setDataEntryForm(formY);
    List<Program> withFormY = programStore.getByDataEntryForm(formY);
    assertEquals(1, withFormY.size());
    assertEquals(programC, withFormY.get(0));
  }

  @Test
  void testGetAndDeleteProgramWithCategoryMappings() {
    ProgramCategoryOptionMapping omA =
        ProgramCategoryOptionMapping.builder().optionId("PWoocil1Oof").filter("Filter A").build();
    ProgramCategoryOptionMapping omB =
        ProgramCategoryOptionMapping.builder().optionId("dEeluoqu2ai").filter("Filter B").build();
    ProgramCategoryOptionMapping omC =
        ProgramCategoryOptionMapping.builder().optionId("Oiewaenai0E").filter("Filter C").build();
    ProgramCategoryOptionMapping omD =
        ProgramCategoryOptionMapping.builder().optionId("lAedahy6eye").filter("Filter D").build();
    List<ProgramCategoryOptionMapping> omList1 = List.of(omA, omB);
    List<ProgramCategoryOptionMapping> omList2 = List.of(omC, omD);
    ProgramCategoryMapping cm1 =
        ProgramCategoryMapping.builder()
            .id("iOChed1vei4")
            .categoryId("Proh3kafa6K")
            .mappingName("Mapping 1")
            .optionMappings(omList1)
            .build();
    ProgramCategoryMapping cm2 =
        ProgramCategoryMapping.builder()
            .id("fshoocuL0sh")
            .categoryId("Oieth9ahGhu")
            .mappingName("Mapping 2")
            .optionMappings(omList2)
            .build();
    Set<ProgramCategoryMapping> categoryMappings = Set.of(cm1, cm2);
    programA.setCategoryMappings(categoryMappings);
    programStore.save(programA);

    Program result = programStore.getByUid(programA.getUid());
    assertEquals(categoryMappings, result.getCategoryMappings());

    programStore.delete(result);
    Program result2 = programStore.getByUid(programA.getUid());
    assertNull(result2);
  }
}
