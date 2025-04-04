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
package org.hisp.dhis.dxf2.metadata.collection;

import java.util.Collection;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjects;
import org.hisp.dhis.feedback.BadRequestException;
import org.hisp.dhis.feedback.ConflictException;
import org.hisp.dhis.feedback.ForbiddenException;
import org.hisp.dhis.feedback.NotFoundException;
import org.hisp.dhis.feedback.TypeReport;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public interface CollectionService {
  TypeReport addCollectionItems(
      IdentifiableObject object,
      String propertyName,
      Collection<? extends IdentifiableObject> objects)
      throws ForbiddenException, ConflictException, NotFoundException, BadRequestException;

  TypeReport delCollectionItems(
      IdentifiableObject object,
      String propertyName,
      Collection<? extends IdentifiableObject> objects)
      throws ForbiddenException, ConflictException, NotFoundException, BadRequestException;

  TypeReport replaceCollectionItems(
      IdentifiableObject object,
      String propertyName,
      Collection<? extends IdentifiableObject> objects)
      throws ForbiddenException, ConflictException, NotFoundException, BadRequestException;

  /**
   * Perform addition and deletion of given {@link IdentifiableObjects} to given {@link
   * IdentifiableObject} in one transaction.
   *
   * @param object {@link IdentifiableObject} to be updated
   * @param propertyName property name of the given {@link IdentifiableObject} which will be
   *     updated.
   * @param items {@link IdentifiableObjects} contains additions and deletions items.
   * @return {@link TypeReport}
   */
  TypeReport mergeCollectionItems(
      IdentifiableObject object, String propertyName, IdentifiableObjects items)
      throws ForbiddenException, ConflictException, NotFoundException, BadRequestException;
}
