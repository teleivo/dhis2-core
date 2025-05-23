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
package org.hisp.dhis.predictor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.expression.Expression;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.test.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lars Helge Overland
 */
@Transactional
class PredictorStoreTest extends PostgresIntegrationTestBase {

  @Autowired private PredictorStore predictorStore;

  @Autowired private DataElementService dataElementService;

  @Autowired private OrganisationUnitService organisationUnitService;

  @Autowired private CategoryService categoryService;

  private OrganisationUnitLevel orgUnitLevel1;

  private DataElement dataElementA;

  private DataElement dataElementB;

  private DataElement dataElementC;

  private DataElement dataElementD;

  private DataElement dataElementX;

  private Set<CategoryOptionCombo> optionCombos;

  private CategoryOptionCombo defaultCombo;

  private Expression expressionA;

  private Expression expressionB;

  private Expression expressionC;

  private Expression expressionD;

  private PeriodType periodType;

  @BeforeEach
  void setUp() {
    orgUnitLevel1 = new OrganisationUnitLevel(1, "Level1");
    organisationUnitService.addOrganisationUnitLevel(orgUnitLevel1);
    dataElementA = createDataElement('A');
    dataElementB = createDataElement('B');
    dataElementC = createDataElement('C');
    dataElementD = createDataElement('D');
    dataElementX = createDataElement('X');
    dataElementService.addDataElement(dataElementA);
    dataElementService.addDataElement(dataElementB);
    dataElementService.addDataElement(dataElementC);
    dataElementService.addDataElement(dataElementD);
    dataElementService.addDataElement(dataElementX);
    CategoryOptionCombo categoryOptionCombo = categoryService.getDefaultCategoryOptionCombo();
    defaultCombo = categoryOptionCombo;
    optionCombos = new HashSet<>();
    optionCombos.add(categoryOptionCombo);
    expressionA = new Expression("expressionA", "descriptionA");
    expressionB = new Expression("expressionB", "descriptionB");
    expressionC = new Expression("expressionC", "descriptionC");
    expressionD = new Expression("expressionD", "descriptionD");
    periodType = PeriodType.getAvailablePeriodTypes().iterator().next();
  }

  // -------------------------------------------------------------------------
  // predictor
  // -------------------------------------------------------------------------
  @Test
  void testSavePredictor() {
    Predictor predictorA =
        createPredictor(
            dataElementX,
            defaultCombo,
            "A",
            expressionA,
            expressionB,
            periodType,
            orgUnitLevel1,
            6,
            1,
            0);
    Predictor predictorB =
        createPredictor(
            dataElementX,
            defaultCombo,
            "B",
            expressionC,
            expressionD,
            periodType,
            orgUnitLevel1,
            6,
            1,
            0);
    predictorStore.save(predictorA);
    long idA = predictorA.getId();
    Set<OrganisationUnitLevel> levelsA = predictorA.getOrganisationUnitLevels();
    Set<OrganisationUnitLevel> expectedLevelsA = new HashSet<>();
    expectedLevelsA.add(orgUnitLevel1);
    predictorA = predictorStore.get(idA);
    levelsA = predictorA.getOrganisationUnitLevels();
    assertEquals(predictorA.getName(), "PredictorA");
    assertEquals(predictorA.getDescription(), "DescriptionA");
    assertNotNull(predictorA.getGenerator().getExpression());
    // TODO Need a good skipTest test
    assertEquals(predictorA.getPeriodType(), periodType);
    assertEquals(predictorA.getOutput(), dataElementX);
    assertEquals(predictorA.getAnnualSampleCount(), Integer.valueOf(0));
    assertEquals(predictorA.getSequentialSampleCount(), Integer.valueOf(6));
    assertEquals(predictorA.getSequentialSkipCount(), Integer.valueOf(1));
    assertEquals(levelsA.size(), 1);
    assertEquals(levelsA, expectedLevelsA);
    predictorStore.save(predictorB);
    long idB = predictorB.getId();
    Set<OrganisationUnitLevel> levelsB = predictorB.getOrganisationUnitLevels();
    Set<OrganisationUnitLevel> expectedLevelsB = new HashSet<>();
    expectedLevelsB.add(orgUnitLevel1);
    predictorB = predictorStore.get(idB);
    levelsB = predictorB.getOrganisationUnitLevels();
    assertEquals(predictorA.getName(), "PredictorA");
    assertEquals(predictorA.getDescription(), "DescriptionA");
    assertNotNull(predictorA.getGenerator().getExpression());
    // TODO Need a good skipTest test
    assertEquals(predictorA.getPeriodType(), periodType);
    assertEquals(predictorA.getOutput(), dataElementX);
    assertEquals(predictorA.getAnnualSampleCount(), Integer.valueOf(0));
    assertEquals(predictorA.getSequentialSampleCount(), Integer.valueOf(6));
    assertEquals(predictorA.getSequentialSkipCount(), Integer.valueOf(1));
    assertEquals(levelsB.size(), 1);
    assertEquals(levelsB, expectedLevelsB);
  }

  @Test
  void testUpdatePredictor() {
    Predictor predictor =
        createPredictor(
            dataElementX,
            defaultCombo,
            "A",
            expressionA,
            expressionB,
            periodType,
            orgUnitLevel1,
            6,
            1,
            0);
    predictorStore.save(predictor);
    long id = predictor.getId();
    predictor = predictorStore.get(id);
    assertEquals(predictor.getName(), "PredictorA");
    assertEquals(predictor.getDescription(), "DescriptionA");
    assertNotNull(predictor.getGenerator().getExpression());
    assertEquals(predictor.getPeriodType(), periodType);
    predictor.setName("PredictorB");
    predictor.setDescription("DescriptionB");
    predictor.setSequentialSkipCount(2);
    predictorStore.update(predictor);
    predictor = predictorStore.get(id);
    assertEquals(predictor.getName(), "PredictorB");
    assertEquals(predictor.getDescription(), "DescriptionB");
    assertEquals(predictor.getSequentialSkipCount(), Integer.valueOf(2));
  }

  @Test
  void testDeletePredictor() {
    Predictor predictorA =
        createPredictor(
            dataElementX,
            defaultCombo,
            "A",
            expressionA,
            expressionB,
            periodType,
            orgUnitLevel1,
            6,
            1,
            0);
    Predictor predictorB =
        createPredictor(
            dataElementX,
            defaultCombo,
            "B",
            expressionC,
            expressionD,
            periodType,
            orgUnitLevel1,
            6,
            1,
            0);
    predictorStore.save(predictorA);
    long idA = predictorA.getId();
    predictorStore.save(predictorB);
    long idB = predictorB.getId();
    assertNotNull(predictorStore.get(idA));
    assertNotNull(predictorStore.get(idB));
    predictorA.clearExpressions();
    predictorStore.delete(predictorA);
    assertNull(predictorStore.get(idA));
    assertNotNull(predictorStore.get(idB));
    predictorB.clearExpressions();
    predictorStore.delete(predictorB);
    assertNull(predictorStore.get(idA));
    assertNull(predictorStore.get(idB));
  }

  @Test
  void testGetAllPredictors() {
    Predictor predictorA =
        createPredictor(
            dataElementX,
            defaultCombo,
            "A",
            expressionA,
            expressionB,
            periodType,
            orgUnitLevel1,
            6,
            1,
            0);
    Predictor predictorB =
        createPredictor(
            dataElementX,
            defaultCombo,
            "B",
            expressionC,
            expressionD,
            periodType,
            orgUnitLevel1,
            6,
            1,
            0);
    predictorStore.save(predictorA);
    predictorStore.save(predictorB);
    List<Predictor> rules = predictorStore.getAll();
    assertEquals(2, rules.size());
    assertTrue(rules.contains(predictorA));
    assertTrue(rules.contains(predictorB));
  }

  @Test
  void testGetPredictorByName() {
    Predictor predictorA =
        createPredictor(
            dataElementX,
            defaultCombo,
            "A",
            expressionA,
            expressionB,
            periodType,
            orgUnitLevel1,
            6,
            1,
            0);
    Predictor predictorB =
        createPredictor(
            dataElementX,
            defaultCombo,
            "B",
            expressionC,
            expressionD,
            periodType,
            orgUnitLevel1,
            6,
            1,
            0);
    predictorStore.save(predictorA);
    long id = predictorA.getId();
    predictorStore.save(predictorB);
    Predictor rule = predictorStore.getByName("PredictorA");
    assertEquals(rule.getId(), id);
    assertEquals(rule.getName(), "PredictorA");
  }

  @Test
  void testGetPredictorCount() {
    Set<DataElement> dataElementsA = new HashSet<>();
    dataElementsA.add(dataElementA);
    dataElementsA.add(dataElementB);
    Set<DataElement> dataElementsB = new HashSet<>();
    dataElementsB.add(dataElementC);
    dataElementsB.add(dataElementD);
    Set<DataElement> dataElementsD = new HashSet<>();
    dataElementsD.addAll(dataElementsA);
    dataElementsD.addAll(dataElementsB);
    Predictor predictorA =
        createPredictor(
            dataElementX,
            defaultCombo,
            "A",
            expressionA,
            expressionB,
            periodType,
            orgUnitLevel1,
            6,
            1,
            0);
    Predictor predictorB =
        createPredictor(
            dataElementX,
            defaultCombo,
            "B",
            expressionC,
            expressionD,
            periodType,
            orgUnitLevel1,
            6,
            1,
            0);
    Expression generator = new Expression("expressionE", "expressionE");
    Expression skipTest = new Expression("expressionF", "expressionF");
    Predictor predictorC =
        createPredictor(
            dataElementX,
            defaultCombo,
            "C",
            generator,
            skipTest,
            periodType,
            orgUnitLevel1,
            6,
            1,
            0);
    predictorStore.save(predictorA);
    predictorStore.save(predictorB);
    predictorStore.save(predictorC);
    assertEquals(3, predictorStore.getCount());
  }

  @Test
  @DisplayName("Retrieving predictors by data element returns expected entries")
  void getIndicatorsWithDenominatorRef() {
    // given
    Predictor predictor1 =
        createPredictor(
            dataElementA,
            defaultCombo,
            "1",
            expressionA,
            expressionB,
            periodType,
            orgUnitLevel1,
            1,
            1,
            1);
    predictorStore.save(predictor1);

    Predictor predictor2 =
        createPredictor(
            dataElementA,
            defaultCombo,
            "2",
            expressionB,
            expressionC,
            periodType,
            orgUnitLevel1,
            1,
            1,
            1);

    predictorStore.save(predictor1);
    predictorStore.save(predictor2);

    predictorStore.save(predictor1);
    predictorStore.save(predictor2);

    // when
    List<Predictor> predictors = predictorStore.getAllByDataElement(List.of(dataElementA));

    // then
    assertEquals(2, predictors.size());
    assertTrue(predictors.containsAll(List.of(predictor1, predictor2)));
  }

  @Test
  @DisplayName(
      "Retrieving Predictors whose generator contains DataElements returns expected results")
  void generatorWithDataElementTest() {
    // given
    DataElement de1 = createDataElement('1');
    DataElement de2 = createDataElement('2');
    dataElementService.addDataElement(de1);
    dataElementService.addDataElement(de2);

    Predictor p1 =
        createPredictor(
            dataElementX,
            defaultCombo,
            "2",
            createExpression2('a', "#{test123.%s}".formatted(de1.getUid())),
            expressionC,
            periodType,
            orgUnitLevel1,
            1,
            1,
            1);

    Predictor p2 =
        createPredictor(
            dataElementX,
            defaultCombo,
            "3",
            createExpression2('a', "#{test123.%s}".formatted(de2.getUid())),
            expressionD,
            periodType,
            orgUnitLevel1,
            1,
            1,
            1);

    predictorStore.save(p1);
    predictorStore.save(p2);

    // when
    List<Predictor> allWithGeneratorDEs =
        predictorStore.getAllWithGeneratorContainingDataElement(
            List.of(de1.getUid(), de2.getUid()));

    // then
    assertEquals(2, allWithGeneratorDEs.size());
    assertTrue(
        allWithGeneratorDEs.containsAll(List.of(p1, p2)),
        "Retrieved result set should contain both Predictors");
  }

  @Test
  @DisplayName(
      "Retrieving Predictors whose sample skip test contains DataElements returns expected results")
  void sampleSkipTestWithDataElementTest() {
    // given
    DataElement de1 = createDataElement('1');
    DataElement de2 = createDataElement('2');
    dataElementService.addDataElement(de1);
    dataElementService.addDataElement(de2);

    Predictor p1 =
        createPredictor(
            dataElementX,
            defaultCombo,
            "2",
            expressionC,
            createExpression2('a', "#{test123.%s}".formatted(de1.getUid())),
            periodType,
            orgUnitLevel1,
            1,
            1,
            1);

    Predictor p2 =
        createPredictor(
            dataElementX,
            defaultCombo,
            "3",
            expressionD,
            createExpression2('a', "#{test123.%s}".formatted(de2.getUid())),
            periodType,
            orgUnitLevel1,
            1,
            1,
            1);

    predictorStore.save(p1);
    predictorStore.save(p2);

    // when
    List<Predictor> allWithSampleSkipTestDEs =
        predictorStore.getAllWithSampleSkipTestContainingDataElement(
            List.of(de1.getUid(), de2.getUid()));

    // then
    assertEquals(2, allWithSampleSkipTestDEs.size());
    assertTrue(
        allWithSampleSkipTestDEs.containsAll(List.of(p1, p2)),
        "Retrieved result set should contain both Predictors");
  }

  @Test
  @DisplayName("Retrieving Predictors by CategoryOptionCombo returns expected results")
  void getByCocTest() {
    // given
    CategoryOptionCombo coc1 = createCategoryOptionCombo('1');
    coc1.setCategoryCombo(categoryService.getDefaultCategoryCombo());
    categoryService.addCategoryOptionCombo(coc1);

    CategoryOptionCombo coc2 = createCategoryOptionCombo('2');
    coc2.setCategoryCombo(categoryService.getDefaultCategoryCombo());
    categoryService.addCategoryOptionCombo(coc2);

    CategoryOptionCombo coc3 = createCategoryOptionCombo('3');
    coc3.setCategoryCombo(categoryService.getDefaultCategoryCombo());
    categoryService.addCategoryOptionCombo(coc3);

    Predictor p1 =
        createPredictor(
            dataElementA,
            coc1,
            "A",
            expressionC,
            createExpression2('a', "#{test123}"),
            periodType,
            orgUnitLevel1,
            1,
            1,
            1);

    Predictor p2 =
        createPredictor(
            dataElementX,
            coc2,
            "B",
            expressionD,
            createExpression2('a', "#{test123}"),
            periodType,
            orgUnitLevel1,
            1,
            1,
            1);

    Predictor p3 =
        createPredictor(
            dataElementB,
            coc3,
            "C",
            expressionB,
            createExpression2('a', "#{test123}"),
            periodType,
            orgUnitLevel1,
            1,
            1,
            1);

    predictorStore.save(p1);
    predictorStore.save(p2);
    predictorStore.save(p3);

    // when
    List<Predictor> allByCoc =
        predictorStore.getByCategoryOptionCombo(UID.of(coc1.getUid(), coc2.getUid()));

    // then
    assertEquals(2, allByCoc.size());
    assertTrue(
        allByCoc.containsAll(List.of(p1, p2)),
        "Retrieved result set should contain both Predictors");
  }
}
