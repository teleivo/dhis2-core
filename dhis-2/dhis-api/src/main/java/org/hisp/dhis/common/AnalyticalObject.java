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

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hisp.dhis.category.CategoryOptionGroupSetDimension;
import org.hisp.dhis.dataelement.DataElementGroupSetDimension;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSetDimension;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.trackedentity.TrackedEntityDataElementDimension;

/**
 * @author Lars Helge Overland
 */
public interface AnalyticalObject
    extends IdentifiableObject, InterpretableObject, SubscribableObject {
  void populateAnalyticalProperties();

  List<DimensionalObject> getColumns();

  List<DimensionalObject> getRows();

  List<DimensionalObject> getFilters();

  Map<String, String> getParentGraphMap();

  Date getRelativePeriodDate();

  OrganisationUnit getRelativeOrganisationUnit();

  List<Period> getPeriods();

  List<OrganisationUnit> getOrganisationUnits();

  boolean addDataDimensionItem(DimensionalItemObject object);

  boolean removeDataDimensionItem(DimensionalItemObject object);

  void addDataElementGroupSetDimension(DataElementGroupSetDimension dimension);

  void addOrganisationUnitGroupSetDimension(OrganisationUnitGroupSetDimension dimension);

  void addCategoryOptionGroupSetDimension(CategoryOptionGroupSetDimension dimension);

  void addTrackedEntityDataElementDimension(TrackedEntityDataElementDimension dimension);

  boolean isCompletedOnly();

  String getTimeField();

  String getOrgUnitField();

  String getTitle();

  boolean hasUserOrgUnit();

  void clearTransientState();
}
