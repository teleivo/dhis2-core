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
package org.hisp.dhis.dxf2.metadata.attribute;

import static org.hisp.dhis.test.TestBase.createOrganisationUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.hisp.dhis.attribute.Attribute;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundle;
import org.hisp.dhis.dxf2.metadata.objectbundle.validation.GeoJsonAttributesCheck;
import org.hisp.dhis.dxf2.metadata.objectbundle.validation.ValidationContext;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ObjectReport;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.preheat.Preheat;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;

/**
 * @author viet@dhis2.org
 */
class GeoJsonAttributesCheckTest {
  private final GeoJsonAttributesCheck geoJsonAttributesCheck = new GeoJsonAttributesCheck();

  private OrganisationUnit organisationUnit;

  private ObjectBundle objectBundle;

  private ValidationContext validationContext;

  private Attribute attribute;

  @BeforeEach
  public void setUpTest() {
    organisationUnit = createOrganisationUnit('A');
    attribute = new Attribute();
    attribute.setUid("geoJson");
    attribute.setName("geoJson");

    validationContext = Mockito.mock(ValidationContext.class);
    Schema schema = new Schema(OrganisationUnit.class, "organisationUnit", "organisationUnits");
    Property property = new Property();
    property.setPersisted(true);
    schema.getPropertyMap().put("attributeValues", property);
    SchemaService schemaService = Mockito.mock(SchemaService.class);

    when(schemaService.getDynamicSchema(OrganisationUnit.class)).thenReturn(schema);
    when(validationContext.getSchemaService()).thenReturn(schemaService);

    Preheat preheat = Mockito.mock(Preheat.class);
    when(preheat.getAttributeIdsByValueType(OrganisationUnit.class, ValueType.GEOJSON))
        .thenReturn(Sets.newSet("geoJson"));

    objectBundle = Mockito.mock(ObjectBundle.class);
    when(objectBundle.getPreheat()).thenReturn(preheat);
  }

  @Test
  public void testValidPolygon() {
    organisationUnit.addAttributeValue(
        attribute.getUid(),
        """
                 {
                         "type": "Polygon",
                         "coordinates": [
                             [
                                 [100.0, 0.0],
                                 [101.0, 0.0],
                                 [101.0, 1.0],
                                 [100.0, 1.0],
                                 [100.0, 0.0]
                             ]
                         ]
                     }""");

    List<ObjectReport> objectReportList = new ArrayList<>();

    geoJsonAttributesCheck.check(
        objectBundle,
        OrganisationUnit.class,
        Lists.newArrayList(organisationUnit),
        Collections.emptyList(),
        ImportStrategy.CREATE_AND_UPDATE,
        validationContext,
        objectReportList::add);

    assertTrue(CollectionUtils.isEmpty(objectReportList));
  }

  @Test
  public void testInValidPolygon() {
    organisationUnit.addAttributeValue(
        attribute.getUid(), " {\n" + "         \"type\": \"Polygon\"     }");

    List<ObjectReport> objectReportList = new ArrayList<>();

    geoJsonAttributesCheck.check(
        objectBundle,
        OrganisationUnit.class,
        Lists.newArrayList(organisationUnit),
        Collections.emptyList(),
        ImportStrategy.CREATE_AND_UPDATE,
        validationContext,
        objectReportList::add);

    assertFalse(CollectionUtils.isEmpty(objectReportList));
    assertEquals(ErrorCode.E6004, objectReportList.get(0).getErrorReports().get(0).getErrorCode());
  }

  @Test
  public void testInValidPolygonType() {
    organisationUnit.addAttributeValue(
        attribute.getUid(), " {\n" + "         \"type\": \"PolygonNew\"     }");

    List<ObjectReport> objectReportList = new ArrayList<>();

    geoJsonAttributesCheck.check(
        objectBundle,
        OrganisationUnit.class,
        Lists.newArrayList(organisationUnit),
        Collections.emptyList(),
        ImportStrategy.CREATE_AND_UPDATE,
        validationContext,
        objectReportList::add);

    assertFalse(CollectionUtils.isEmpty(objectReportList));
    assertEquals(ErrorCode.E6004, objectReportList.get(0).getErrorReports().get(0).getErrorCode());
  }

  @Test
  public void testInvalidPolygonCoordinates() {
    organisationUnit.addAttributeValue(
        attribute.getUid(),
        """
                 {
                         "type": "Polygon",
                         "test": [
                             [
                                 [100.0, 0.0],
                                 [101.0, 0.0],
                                 [101.0, 1.0],
                                 [100.0, 1.0],
                                 [100.0, 0.0]
                             ]
                         ]
                     }""");

    List<ObjectReport> objectReportList = new ArrayList<>();

    geoJsonAttributesCheck.check(
        objectBundle,
        OrganisationUnit.class,
        Lists.newArrayList(organisationUnit),
        Collections.emptyList(),
        ImportStrategy.CREATE_AND_UPDATE,
        validationContext,
        objectReportList::add);
    assertFalse(CollectionUtils.isEmpty(objectReportList));
    assertEquals(ErrorCode.E6004, objectReportList.get(0).getErrorReports().get(0).getErrorCode());
  }

  @Test
  public void testInvalidPolygonCoordinatesValue() {
    organisationUnit.addAttributeValue(
        attribute.getUid(),
        """
                {
                 "type": "Polygon",         "coordinates": [100.0, 0.0]     }""");

    List<ObjectReport> objectReportList = new ArrayList<>();

    geoJsonAttributesCheck.check(
        objectBundle,
        OrganisationUnit.class,
        Lists.newArrayList(organisationUnit),
        Collections.emptyList(),
        ImportStrategy.CREATE_AND_UPDATE,
        validationContext,
        objectReportList::add);
    assertFalse(CollectionUtils.isEmpty(objectReportList));
    assertEquals(ErrorCode.E6004, objectReportList.get(0).getErrorReports().get(0).getErrorCode());
  }

  @Test
  public void testInvalidGeoJsonType() {
    String geoJson =
        """
            {
              "type": "Feature",
              "geometry": { "type": "Point","coordinasstes": [125.6, 10.1] },
              "properties": { "name": "Dinagat Islands" }
            }""";

    organisationUnit.addAttributeValue(attribute.getUid(), geoJson);

    List<ObjectReport> objectReportList = new ArrayList<>();

    geoJsonAttributesCheck.check(
        objectBundle,
        OrganisationUnit.class,
        Lists.newArrayList(organisationUnit),
        Collections.emptyList(),
        ImportStrategy.CREATE_AND_UPDATE,
        validationContext,
        objectReportList::add);
    assertFalse(CollectionUtils.isEmpty(objectReportList));
    assertEquals(ErrorCode.E6005, objectReportList.get(0).getErrorReports().get(0).getErrorCode());
  }
}
