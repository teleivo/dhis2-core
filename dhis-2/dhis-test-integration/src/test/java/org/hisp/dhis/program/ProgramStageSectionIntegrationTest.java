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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Sets;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashSet;
import org.hisp.dhis.category.CategoryCombo;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dbms.DbmsManager;
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
class ProgramStageSectionIntegrationTest extends PostgresIntegrationTestBase {

  @Autowired private ProgramStageService programStageService;

  @Autowired private ProgramStageSectionService programStageSectionService;

  @Autowired private ProgramService programService;

  @Autowired private DataElementService dataElementService;

  @Autowired private OrganisationUnitService organisationUnitService;

  @Autowired private DbmsManager dbmsManager;

  @PersistenceContext private EntityManager entityManager;

  private Program program;

  private ProgramStage stageA;

  private ProgramStageSection sectionA;

  private ProgramStageDataElement programStageDataElementA;

  @BeforeEach
  void setUp() {
    OrganisationUnit organisationUnit = createOrganisationUnit('A');
    organisationUnitService.addOrganisationUnit(organisationUnit);
    sectionA = createProgramStageSection('A', 1);
    programStageSectionService.saveProgramStageSection(sectionA);
    CategoryCombo categoryCombo = createCategoryCombo('A');
    categoryService.addCategoryCombo(categoryCombo);
    DataElement dataElementA = createDataElement('A', categoryCombo);
    dataElementService.addDataElement(dataElementA);
    programStageDataElementA = createProgramStageDataElement(stageA, dataElementA, 1);
    program = createProgram('A', new HashSet<>(), organisationUnit);
    programService.addProgram(program);
    stageA = new ProgramStage("A", program);
    stageA.setUid("UID-A");
    stageA.setProgramStageSections(Sets.newHashSet(sectionA));
    stageA.setProgramStageDataElements(Sets.newHashSet(programStageDataElementA));
  }

  @Test
  void testRemoveProgramStageSectionWillDeleteOrphans() {
    long idA = programStageService.saveProgramStage(stageA);
    assertNotNull(programStageService.getProgramStage(idA));
    long sectionId = stageA.getProgramStageSections().stream().findFirst().get().getId();
    assertNotNull(programStageSectionService.getProgramStageSection(sectionId));
    stageA.getProgramStageSections().clear();
    programStageService.updateProgramStage(stageA);
    dbmsManager.flushSession();

    assertTrue(entityManager.find(ProgramStage.class, idA).getProgramStageSections().isEmpty());
    assertNull(entityManager.find(ProgramStageSection.class, sectionId));
  }
}
