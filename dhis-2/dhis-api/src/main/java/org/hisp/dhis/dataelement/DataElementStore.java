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
package org.hisp.dhis.dataelement;

import java.util.List;
import org.hisp.dhis.category.CategoryCombo;
import org.hisp.dhis.common.GenericDimensionalObjectStore;
import org.hisp.dhis.user.User;

/**
 * Defines the functionality for persisting DataElements and DataElementGroups.
 *
 * @author Torgeir Lorange Ostby
 */
public interface DataElementStore extends GenericDimensionalObjectStore<DataElement> {
  String ID = DataElementStore.class.getName();

  // -------------------------------------------------------------------------
  // DataElement
  // -------------------------------------------------------------------------

  /**
   * Returns all DataElements with the given category combo.
   *
   * @param categoryCombo the CategoryCombo.
   * @return all DataElements with the given category combo.
   */
  List<DataElement> getDataElementByCategoryCombo(CategoryCombo categoryCombo);

  /**
   * Returns all DataElements which are not member of any DataElementGroups.
   *
   * @return all DataElements which are not member of any DataElementGroups.
   */
  List<DataElement> getDataElementsWithoutGroups();

  /**
   * Returns all DataElements which are not assigned to any DataSets.
   *
   * @return all DataElements which are not assigned to any DataSets.
   */
  List<DataElement> getDataElementsWithoutDataSets();

  /**
   * Returns all DataElements which are assigned to at least one DataSet.
   *
   * @return all DataElements which are assigned to at least one DataSet.
   */
  List<DataElement> getDataElementsWithDataSets();

  /**
   * Returns all DataElements which have the given aggregation level assigned.
   *
   * @param aggregationLevel the aggregation level.
   * @return all DataElements which have the given aggregation level assigned.
   */
  List<DataElement> getDataElementsByAggregationLevel(int aggregationLevel);

  /**
   * Returns all DataElements which the user hav access to.
   *
   * <p>NOTE: it should only be used in tests, should not be used in production code!
   *
   * @param uid data element uid.
   * @return all DataElements which the user has access to.
   */
  DataElement getDataElement(String uid, User user);
}
