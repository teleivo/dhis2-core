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
package org.hisp.dhis.dxf2.adx;

import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.hisp.dhis.common.CodeGenerator.isValidUid;
import static org.hisp.dhis.common.collection.CollectionUtils.isEmpty;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.util.XMLChar;
import org.hisp.dhis.category.Category;
import org.hisp.dhis.category.CategoryCombo;
import org.hisp.dhis.category.CategoryComboMap;
import org.hisp.dhis.category.CategoryComboMap.CategoryComboMapException;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.common.IdScheme;
import org.hisp.dhis.common.IdSchemes;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.commons.collection.CachingMap;
import org.hisp.dhis.commons.util.DebugUtils;
import org.hisp.dhis.commons.util.StreamUtils;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.datavalue.DataExportParams;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.datavalueset.DataValueSetQueryParams;
import org.hisp.dhis.dxf2.datavalueset.DataValueSetService;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.scheduling.JobProgress;
import org.hisp.dhis.system.callable.IdentifiableObjectCallable;
import org.hisp.staxwax.factory.XMLFactory;
import org.hisp.staxwax.reader.XMLReader;
import org.hisp.staxwax.writer.XMLWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author bobj
 */
@Slf4j
@RequiredArgsConstructor
@Service("org.hisp.dhis.dxf2.AdxDataService")
public class DefaultAdxDataService implements AdxDataService {

  private final DataValueSetService dataValueSetService;
  private final DataValueService dataValueService;
  private final PeriodService periodService;
  private final IdentifiableObjectManager identifiableObjectManager;

  @Override
  public DataExportParams getFromUrl(DataValueSetQueryParams urlParams) {
    IdSchemes outputIdSchemes = urlParams.getOutputIdSchemes();
    outputIdSchemes.setDefaultIdScheme(IdScheme.CODE);

    DataExportParams params = new DataExportParams();

    if (!isEmpty(urlParams.getDataSet())) {
      params.getDataSets().addAll(getByUidOrCode(DataSet.class, urlParams.getDataSet()));
    }
    if (!isEmpty(urlParams.getDataElement())) {
      params
          .getDataElements()
          .addAll(getByUidOrCode(DataElement.class, urlParams.getDataElement()));
    }
    if (!isEmpty(urlParams.getPeriod())) {
      params
          .getPeriods()
          .addAll(periodService.reloadIsoPeriods(new ArrayList<>(urlParams.getPeriod())));
    } else if (urlParams.getStartDate() != null && urlParams.getEndDate() != null) {
      params.setStartDate(urlParams.getStartDate());
      params.setEndDate(urlParams.getEndDate());
    }

    if (!isEmpty(urlParams.getOrgUnit())) {
      params
          .getOrganisationUnits()
          .addAll(getByUidOrCode(OrganisationUnit.class, urlParams.getOrgUnit()));
    }

    if (!isEmpty(urlParams.getOrgUnitGroup())) {
      params
          .getOrganisationUnitGroups()
          .addAll(getByUidOrCode(OrganisationUnitGroup.class, urlParams.getOrgUnitGroup()));
    }

    if (!isEmpty(urlParams.getAttributeOptionCombo())) {
      params
          .getAttributeOptionCombos()
          .addAll(getByUidOrCode(CategoryOptionCombo.class, urlParams.getAttributeOptionCombo()));
    }

    params.setIncludeDescendants(urlParams.isChildren());
    params.setIncludeDeleted(urlParams.isIncludeDeleted());
    params.setLastUpdated(urlParams.getLastUpdated());
    params.setLastUpdatedDuration(urlParams.getLastUpdatedDuration());
    params.setLimit(urlParams.getLimit());
    params.setOutputIdSchemes(outputIdSchemes);

    return params;
  }

  @Override
  public void writeDataValueSet(DataExportParams params, OutputStream out) throws AdxException {
    dataValueSetService.decideAccess(params);
    dataValueSetService.validate(params);

    XMLWriter adxWriter = XMLFactory.getXMLWriter(out);

    adxWriter.openElement(AdxDataService.ROOT);
    adxWriter.writeAttribute("xmlns", AdxDataService.NAMESPACE);

    IdSchemes idSchemes = params.getOutputIdSchemes();
    IdScheme ouScheme = idSchemes.getOrgUnitIdScheme();
    IdScheme dsScheme = idSchemes.getDataSetIdScheme();
    IdScheme deScheme = idSchemes.getDataElementIdScheme();

    for (DataSet dataSet : params.getDataSets()) {
      AdxDataSetMetadata metadata = new AdxDataSetMetadata(dataSet, idSchemes);

      for (CategoryOptionCombo aoc : getAttribuetOptionCombos(dataSet, params)) {
        Map<String, String> attributeDimensions =
            metadata.getExplodedCategoryAttributes(aoc.getId());

        for (OrganisationUnit orgUnit : params.getAllOrganisationUnits()) {
          Period currentPeriod = null;
          OrganisationUnit currentOrgUnit = null;

          DataExportParams queryParams =
              new DataExportParams()
                  .setDataElements(dataSet.getDataElements())
                  .setOrganisationUnits(Sets.newHashSet(orgUnit))
                  .setIncludeDescendants(params.isIncludeDescendants())
                  .setIncludeDeleted(params.isIncludeDeleted())
                  .setLastUpdated(params.getLastUpdated())
                  .setLastUpdatedDuration(params.getLastUpdatedDuration())
                  .setPeriods(params.getPeriods())
                  .setStartDate(params.getStartDate())
                  .setEndDate(params.getEndDate())
                  .setAttributeOptionCombos(Sets.newHashSet(aoc))
                  .setOrderByOrgUnitPath(true)
                  .setOrderByPeriod(true);

          for (DataValue dv : dataValueService.getDataValues(queryParams)) {
            if (!dv.getPeriod().equals(currentPeriod) || !dv.getSource().equals(currentOrgUnit)) {
              if (currentPeriod != null) {
                adxWriter.closeElement(); // GROUP
              }

              currentPeriod = dv.getPeriod();
              currentOrgUnit = dv.getSource();

              adxWriter.openElement(AdxDataService.GROUP);
              adxWriter.writeAttribute(AdxDataService.DATASET, dataSet.getPropertyValue(dsScheme));
              adxWriter.writeAttribute(AdxDataService.PERIOD, AdxPeriod.serialize(currentPeriod));
              adxWriter.writeAttribute(
                  AdxDataService.ORGUNIT, currentOrgUnit.getPropertyValue(ouScheme));

              for (Map.Entry<String, String> e : attributeDimensions.entrySet()) {
                adxWriter.writeAttribute(e.getKey(), e.getValue());
              }
            }
            adxWriter.openElement(AdxDataService.DATAVALUE);

            adxWriter.writeAttribute(
                AdxDataService.DATAELEMENT, dv.getDataElement().getPropertyValue(deScheme));

            CategoryOptionCombo coc = dv.getCategoryOptionCombo();

            Map<String, String> categoryDimensions =
                metadata.getExplodedCategoryAttributes(coc.getId());

            for (Map.Entry<String, String> e : categoryDimensions.entrySet()) {
              adxWriter.writeAttribute(e.getKey(), e.getValue());
            }

            if (dv.getDataElement().getValueType().isNumeric()) {
              adxWriter.writeAttribute(AdxDataService.VALUE, dv.getValue());
            } else {
              adxWriter.writeAttribute(AdxDataService.VALUE, "0");
              adxWriter.openElement(AdxDataService.ANNOTATION);
              adxWriter.writeCharacters(dv.getValue());
              adxWriter.closeElement(); // ANNOTATION
            }
            adxWriter.closeElement(); // DATAVALUE
          }

          if (currentPeriod != null) {
            adxWriter.closeElement(); // GROUP
          }
        }
      }
    }

    adxWriter.closeElement(); // ADX

    adxWriter.closeWriter();
  }

  @Override
  @Transactional
  public ImportSummary saveDataValueSet(
      InputStream in, ImportOptions importOptions, @Nonnull JobProgress progress) {
    importOptions.getIdSchemes().setDefaultIdScheme(IdScheme.CODE);

    try {
      in = StreamUtils.wrapAndCheckCompressionFormat(in);
      return saveDataValueSetInternal(in, importOptions, progress);
    } catch (IOException ex) {
      log.warn("Import failed: " + DebugUtils.getStackTrace(ex));
      return new ImportSummary(ImportStatus.ERROR, "ADX import failed");
    }
  }

  // -------------------------------------------------------------------------
  // Supportive methods
  // -------------------------------------------------------------------------

  private <T extends IdentifiableObject> List<T> getByUidOrCode(Class<T> clazz, Set<String> ids) {
    return ids.stream()
        .map(id -> getByUidOrCode(clazz, id))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private <T extends IdentifiableObject> T getByUidOrCode(Class<T> clazz, String id) {
    if (isValidUid(id)) {
      T object = identifiableObjectManager.get(clazz, id);

      if (object != null) {
        return object;
      }
    }

    return identifiableObjectManager.getByCode(clazz, id);
  }

  private Set<CategoryOptionCombo> getAttribuetOptionCombos(
      DataSet dataSet, DataExportParams params) {
    Set<CategoryOptionCombo> aocs = dataSet.getCategoryCombo().getOptionCombos();

    if (params.hasAttributeOptionCombos()) {
      aocs = new HashSet<>(aocs);

      aocs.retainAll(params.getAttributeOptionCombos());
    }

    return aocs;
  }

  private ImportSummary saveDataValueSetInternal(
      InputStream in, ImportOptions importOptions, @Nonnull JobProgress progress) {
    // Get import options
    IdScheme dsScheme = importOptions.getIdSchemes().getDataSetIdScheme();
    IdScheme deScheme = importOptions.getIdSchemes().getDataElementIdScheme();

    // Create meta-data maps
    CachingMap<String, DataSet> dataSetMap = new CachingMap<>();
    CachingMap<String, DataElement> dataElementMap = new CachingMap<>();

    // Get meta-data maps
    IdentifiableObjectCallable<DataSet> dataSetCallable =
        new IdentifiableObjectCallable<>(identifiableObjectManager, DataSet.class, dsScheme, null);
    IdentifiableObjectCallable<DataElement> dataElementCallable =
        new IdentifiableObjectCallable<>(
            identifiableObjectManager, DataElement.class, deScheme, null);

    // Heat cache
    if (importOptions.isPreheatCacheDefaultFalse()) {
      dataSetMap.load(
          identifiableObjectManager.getAll(DataSet.class), o -> o.getPropertyValue(dsScheme));
      dataElementMap.load(
          identifiableObjectManager.getAll(DataElement.class), o -> o.getPropertyValue(deScheme));
    }

    XMLReader adxReader = XMLFactory.getXMLReader(in);

    Consumer<Map<String, String>> handleGroup =
        map -> parseAdxGroupToDxf(map, importOptions, dataSetMap, dataSetCallable);
    Predicate<Map<String, String>> handleValue =
        map -> parseAdxDataValueToDxf(map, importOptions, dataElementMap, dataElementCallable);

    try {
      AdxDataValueSetReader reader = new AdxDataValueSetReader(adxReader, handleGroup, handleValue);
      return dataValueSetService.importDataValueSetAdx(reader, importOptions, progress);
    } catch (Exception ex) {
      ImportSummary importSummary = new ImportSummary();
      importSummary.setStatus(ImportStatus.ERROR);
      importSummary.setDescription("Data set import failed");
      log.warn("Import failed: " + DebugUtils.getStackTrace(ex));
      return importSummary;
    }
  }

  // -------------------------------------------------------------------------
  // Utility methods
  // -------------------------------------------------------------------------

  private static void parseAdxGroupToDxf(
      Map<String, String> groupAttributes,
      ImportOptions importOptions,
      CachingMap<String, DataSet> dataSetMap,
      IdentifiableObjectCallable<DataSet> dataSetCallable) {
    if (!groupAttributes.containsKey(AdxDataService.PERIOD)) {
      throw new IllegalArgumentException(
          AdxDataService.PERIOD + " attribute is required on 'group'");
    }

    if (!groupAttributes.containsKey(AdxDataService.ORGUNIT)) {
      throw new IllegalArgumentException(
          AdxDataService.ORGUNIT + " attribute is required on 'group'");
    }

    // translate ADX period to DXF
    String periodStr = groupAttributes.get(AdxDataService.PERIOD);
    groupAttributes.remove(AdxDataService.PERIOD);
    Period period = AdxPeriod.parse(periodStr);
    groupAttributes.put(AdxDataService.PERIOD, period.getIsoDate());

    // process ADX group attributes
    if (!groupAttributes.containsKey(AdxDataService.ATTOPTCOMBO)
        && groupAttributes.containsKey(AdxDataService.DATASET)) {
      log.debug("No attribute option combo present, check data set for attribute category combo");

      String dataSetStr = trimToNull(groupAttributes.get(AdxDataService.DATASET));
      final DataSet dataSet = dataSetMap.get(dataSetStr, dataSetCallable.setId(dataSetStr));

      if (dataSet == null) {
        throw new IllegalArgumentException(
            "No data set matching "
                + dataSetCallable.getIdScheme().name().toLowerCase()
                + " '"
                + groupAttributes.get(AdxDataService.DATASET)
                + "'");
      }

      groupAttributes.put(AdxDataService.DATASET, dataSet.getUid());
      CategoryCombo attributeCombo = dataSet.getCategoryCombo();
      convertAttributesToDxf(
          groupAttributes,
          AdxDataService.ATTOPTCOMBO,
          attributeCombo,
          importOptions.getIdSchemes());
    }
  }

  private static boolean parseAdxDataValueToDxf(
      Map<String, String> dvAttributes,
      ImportOptions importOptions,
      CachingMap<String, DataElement> dataElementMap,
      IdentifiableObjectCallable<DataElement> dataElementCallable) {

    log.debug("Processing data value: " + dvAttributes);

    if (!dvAttributes.containsKey(AdxDataService.DATAELEMENT)) {
      throw new IllegalArgumentException(
          AdxDataService.DATAELEMENT + " attribute is required on 'dataValue'");
    }

    if (!dvAttributes.containsKey(AdxDataService.VALUE)) {
      throw new IllegalArgumentException(
          AdxDataService.VALUE + " attribute is required on 'dataValue'");
    }

    String dataElementStr = trimToNull(dvAttributes.get(AdxDataService.DATAELEMENT));
    final DataElement dataElement =
        dataElementMap.get(dataElementStr, dataElementCallable.setId(dataElementStr));

    if (dataElement == null) {
      throw new IllegalArgumentException(
          "No data element matching "
              + dataElementCallable.getIdScheme().name().toLowerCase()
              + " '"
              + dataElementStr
              + "'");
    }

    // process ADX data value attributes
    if (!dvAttributes.containsKey(AdxDataService.CATOPTCOMBO)) {
      log.debug("No category option combo present");

      // TODO expand to allow for category combos part of DataSetElements.

      CategoryCombo categoryCombo = dataElement.getCategoryCombo();

      convertAttributesToDxf(
          dvAttributes, AdxDataService.CATOPTCOMBO, categoryCombo, importOptions.getIdSchemes());
    }

    log.debug("Processing data value as DXF: " + dvAttributes);
    return !dataElement.getValueType().isNumeric();
  }

  private static Map<String, Category> getCodeCategoryMap(
      CategoryCombo categoryCombo, IdScheme catScheme) {
    Map<String, Category> categoryMap = new HashMap<>();

    List<Category> categories = categoryCombo.getCategories();

    for (Category category : categories) {
      String categoryId = category.getPropertyValue(catScheme);

      if (categoryId == null || !XMLChar.isValidName(categoryId)) {
        throw new IllegalArgumentException(
            "Category "
                + catScheme.name()
                + " for "
                + category.getName()
                + " is missing or invalid: "
                + categoryId);
      }

      categoryMap.put(categoryId, category);
    }

    return categoryMap;
  }

  private static CategoryOptionCombo getCatOptComboFromAttributes(
      Map<String, String> attributes, CategoryCombo cc, IdSchemes idSchemes) {
    CategoryComboMap catcomboMap;

    try {
      catcomboMap = new CategoryComboMap(cc, idSchemes.getCategoryOptionIdScheme());
    } catch (CategoryComboMapException ex) {
      log.info("Failed to create category combo map from: " + cc);
      throw new IllegalArgumentException(ex.getMessage());
    }

    String compositeIdentifier = StringUtils.EMPTY;

    for (Category category : catcomboMap.getCategories()) {
      String categoryId = category.getPropertyValue(idSchemes.getCategoryIdScheme());

      if (categoryId == null) {
        throw new IllegalArgumentException(
            "No category "
                + idSchemes.getCategoryIdScheme().name()
                + " for: "
                + category.toString());
      }

      String catAttribute = attributes.get(categoryId);

      if (catAttribute == null) {
        throw new IllegalArgumentException(
            "Missing required attribute from category combo " + cc.getName() + ": " + categoryId);
      }

      compositeIdentifier += "\"" + catAttribute + "\"";
    }

    CategoryOptionCombo catOptionCombo = catcomboMap.getCategoryOptionCombo(compositeIdentifier);

    if (catOptionCombo == null) {
      throw new IllegalArgumentException("Invalid attributes: " + attributes);
    }

    return catOptionCombo;
  }

  private static void convertAttributesToDxf(
      Map<String, String> attributes,
      String optionComboName,
      CategoryCombo catCombo,
      IdSchemes idSchemes) {
    log.debug("ADX attributes: " + attributes);

    if (catCombo.isDefault()) {
      return;
    }

    Map<String, Category> categoryMap =
        getCodeCategoryMap(catCombo, idSchemes.getCategoryIdScheme());

    Map<String, String> attributeOptions = new HashMap<>();

    for (String category : categoryMap.keySet()) {
      if (attributes.containsKey(category)) {
        attributeOptions.put(category, attributes.get(category));
        attributes.remove(category);
      } else {
        throw new IllegalArgumentException(
            "Category combo "
                + catCombo.getName()
                + " must have "
                + categoryMap.get(category).getName());
      }
    }

    CategoryOptionCombo catOptCombo =
        getCatOptComboFromAttributes(attributeOptions, catCombo, idSchemes);

    attributes.put(
        optionComboName, catOptCombo.getPropertyValue(idSchemes.getCategoryOptionComboIdScheme()));

    log.debug("DXF attributes: " + attributes);
  }
}
