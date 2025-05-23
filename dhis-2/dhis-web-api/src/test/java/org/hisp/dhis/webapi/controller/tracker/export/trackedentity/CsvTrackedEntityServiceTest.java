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
package org.hisp.dhis.webapi.controller.tracker.export.trackedentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.geotools.geojson.geom.GeometryJSON;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.webapi.controller.tracker.view.Attribute;
import org.hisp.dhis.webapi.controller.tracker.view.TrackedEntity;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

class CsvTrackedEntityServiceTest {
  private static final UID TE_UID = UID.of("h4w96yEMlzO");

  private final CsvTrackedEntityService service = new CsvTrackedEntityService();

  private Instant instant;

  @Test
  void singlePointCoordinatesEntityIsWritten() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TrackedEntity trackedEntity = createTrackedEntity();

    GeometryJSON geometryJSON = new GeometryJSON();
    String pointCoordinates = "[40,5]";
    Geometry geometry =
        geometryJSON.read("{\"type\":\"Point\", \"coordinates\": " + pointCoordinates + " }");
    trackedEntity.setGeometry(geometry);

    service.write(out, List.of(trackedEntity), false);
    assertEquals(
        "h4w96yEMlzO,,"
            + instant.toString()
            + ",,,,\"Test org unit\",false,false,false,\"POINT (40 5)\",5.0,40.0,,,,,,,,,\n",
        out.toString());
  }

  @Test
  void singleLineCoordinatesEntityIsWritten() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TrackedEntity trackedEntity = createTrackedEntity();

    GeometryJSON geometryJSON = new GeometryJSON();
    String lineCoordinates = "[[40,5],[41,6]]";
    Geometry geometry =
        geometryJSON.read("{\"type\":\"LineString\", \"coordinates\": " + lineCoordinates + " }");
    trackedEntity.setGeometry(geometry);

    service.write(out, List.of(trackedEntity), false);
    assertEquals(
        "h4w96yEMlzO,,"
            + instant.toString()
            + ",,,,\"Test org unit\",false,false,false,\"LINESTRING (40 5, 41 6)\",,,,,,,,,,,\n",
        out.toString());
  }

  @Test
  void multipleTrackedEntitiesAreWritten() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    List<TrackedEntity> trackedEntities = new ArrayList<>();
    IntStream.range(0, 3).forEach(i -> trackedEntities.add(createTrackedEntity()));

    GeometryJSON geometryJSON = new GeometryJSON();
    String pointCoordinates = "[40,5]";
    Geometry geometry =
        geometryJSON.read("{\"type\":\"Point\", \"coordinates\": " + pointCoordinates + " }");
    trackedEntities.get(1).setGeometry(geometry);

    service.write(out, trackedEntities, false);
    assertEquals(
        "h4w96yEMlzO,,"
            + instant.toString()
            + ",,,,\"Test org unit\",false,false,false,,,,,,,,,,,,\n"
            + "h4w96yEMlzO,,"
            + instant
            + ",,,,\"Test org unit\",false,false,false,\"POINT (40 5)\",5.0,40.0,,,,,,,,,\n"
            + "h4w96yEMlzO,,"
            + instant
            + ",,,,\"Test org unit\",false,false,false,,,,,,,,,,,,\n",
        out.toString());
  }

  @Test
  void multipleTrackedEntitiesWithAttributesAreWritten() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    List<TrackedEntity> trackedEntities = new ArrayList<>();
    IntStream.range(0, 3).forEach(i -> trackedEntities.add(createTrackedEntity()));

    GeometryJSON geometryJSON = new GeometryJSON();
    String pointCoordinates = "[40,5]";
    Geometry geometry =
        geometryJSON.read("{\"type\":\"Point\", \"coordinates\": " + pointCoordinates + " }");
    trackedEntities.get(1).setGeometry(geometry);

    Attribute attribute1 = createAttribute("attribute 1", ValueType.AGE, "Age test");
    Attribute attribute2 = createAttribute("attribute 2", ValueType.TEXT, "Text test");
    trackedEntities.get(2).setAttributes(Arrays.asList(attribute1, attribute2));

    service.write(out, trackedEntities, false);
    assertEquals(
        "h4w96yEMlzO,,"
            + instant.toString()
            + ",,,,\"Test org unit\",false,false,false,,,,,,,,,,,,\n"
            + "h4w96yEMlzO,,"
            + instant
            + ",,,,\"Test org unit\",false,false,false,\"POINT (40 5)\",5.0,40.0,,,,,,,,,\n"
            + "h4w96yEMlzO,,"
            + instant
            + ",,,,\"Test org unit\",false,false,false,,,,,,,,,\"attribute 1\",,\"Age test\",AGE\n"
            + "h4w96yEMlzO,,"
            + instant
            + ",,,,\"Test org unit\",false,false,false,,,,,,,,,\"attribute 2\",,\"Text test\",TEXT\n",
        out.toString());
  }

  @Test
  void zipFileAndMatchCsvTeWhenCreateZipFileFromTrackedEntityList() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    List<TrackedEntity> trackedEntities = new ArrayList<>();

    trackedEntities.add(getTrackedEntityToCompress());

    service.writeZip(outputStream, trackedEntities, false, "file.json");

    ZipInputStream zipInputStream =
        new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
    var buff = new byte[1024];

    ZipEntry zipEntry = zipInputStream.getNextEntry();

    assertNotNull(zipEntry, "Events Zip file has no entry");
    assertEquals("file.json", zipEntry.getName(), "Events Zip file entry has a wrong name");

    var csvStream = new ByteArrayOutputStream();
    int l;
    while ((l = zipInputStream.read(buff)) > 0) {
      csvStream.write(buff, 0, l);
    }

    assertEquals(
"""
h4w96yEMlzO,,2022-09-29T15:15:30Z,,,,"Test org unit",false,false,false,"POINT (40 5)",5.0,40.0,,,,,,"attribute 1",,"Age test",AGE
h4w96yEMlzO,,2022-09-29T15:15:30Z,,,,"Test org unit",false,false,false,"POINT (40 5)",5.0,40.0,,,,,,"attribute 2",,"Text test",TEXT
""",
        csvStream.toString(),
        "The tracked entity does not match or not exists in the Zip File.");
  }

  @Test
  void gzipFileAndMatchCsvTeWhenCreateGZipFileFromTrackedEntityList() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    List<TrackedEntity> trackedEntities = new ArrayList<>();

    trackedEntities.add(getTrackedEntityToCompress());

    service.writeGzip(outputStream, trackedEntities, false);

    GZIPInputStream gzipInputStream =
        new GZIPInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
    var buff = new byte[1024];

    var csvStream = new ByteArrayOutputStream();
    int l;
    while ((l = gzipInputStream.read(buff)) > 0) {
      csvStream.write(buff, 0, l);
    }

    assertEquals(
"""
h4w96yEMlzO,,2022-09-29T15:15:30Z,,,,"Test org unit",false,false,false,"POINT (40 5)",5.0,40.0,,,,,,"attribute 1",,"Age test",AGE
h4w96yEMlzO,,2022-09-29T15:15:30Z,,,,"Test org unit",false,false,false,"POINT (40 5)",5.0,40.0,,,,,,"attribute 2",,"Text test",TEXT
""",
        csvStream.toString(),
        "The tracked entity does not match or not exists in the Zip File.");
  }

  private TrackedEntity getTrackedEntityToCompress() throws IOException {
    TrackedEntity trackedEntity = createTrackedEntity();

    GeometryJSON geometryJSON = new GeometryJSON();
    String pointCoordinates = "[40,5]";
    Geometry geometry =
        geometryJSON.read("{\"type\":\"Point\", \"coordinates\": " + pointCoordinates + " }");
    trackedEntity.setGeometry(geometry);

    Attribute attribute1 = createAttribute("attribute 1", ValueType.AGE, "Age test");
    Attribute attribute2 = createAttribute("attribute 2", ValueType.TEXT, "Text test");
    trackedEntity.setAttributes(Arrays.asList(attribute1, attribute2));
    return trackedEntity;
  }

  private Attribute createAttribute(String attr, ValueType valueType, String value) {
    Attribute attribute = new Attribute();
    attribute.setAttribute(attr);
    attribute.setValueType(valueType);
    attribute.setValue(value);

    return attribute;
  }

  private TrackedEntity createTrackedEntity() {
    TrackedEntity trackedEntity = new TrackedEntity();
    trackedEntity.setTrackedEntity(TE_UID);
    instant = Instant.parse("2022-09-29T15:15:30.00Z");
    trackedEntity.setCreatedAt(instant);
    trackedEntity.setOrgUnit("Test org unit");

    return trackedEntity;
  }
}
