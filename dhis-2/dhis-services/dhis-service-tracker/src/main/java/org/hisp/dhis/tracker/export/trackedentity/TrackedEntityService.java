/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
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
package org.hisp.dhis.tracker.export.trackedentity;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.feedback.BadRequestException;
import org.hisp.dhis.feedback.ForbiddenException;
import org.hisp.dhis.feedback.NotFoundException;
import org.hisp.dhis.fileresource.ImageFileDimension;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.tracker.Page;
import org.hisp.dhis.tracker.PageParams;
import org.hisp.dhis.tracker.export.FileResourceStream;

public interface TrackedEntityService {

  /** Get a file for a tracked entities' attribute. */
  FileResourceStream getFileResource(UID trackedEntity, UID attribute, UID program)
      throws NotFoundException;

  /** Get an image for a tracked entities' attribute in the given dimension. */
  FileResourceStream getFileResourceImage(
      UID trackedEntity, UID attribute, UID program, ImageFileDimension dimension)
      throws NotFoundException;

  /**
   * Get the tracked entity matching given {@code UID} under the privileges of the currently
   * authenticated user. No program attributes are included, only TETAs. Enrollments and
   * relationships are not included. Use {@link #getTrackedEntity(UID, UID, TrackedEntityParams)}
   * instead to also get the relationships, enrollments and program attributes.
   */
  @Nonnull
  TrackedEntity getNewTrackedEntity(@Nonnull UID uid) throws NotFoundException, ForbiddenException;

  /**
   * Get the tracked entity matching given {@code UID} under the privileges of the currently
   * authenticated user. If {@code program} is defined, program attributes for such program are
   * included, otherwise only TETAs are included. It will include enrollments, relationships,
   * attributes and ownerships as defined in {@code params}.
   */
  @Nonnull
  TrackedEntity getNewTrackedEntity(
      @Nonnull UID uid, UID program, @Nonnull TrackedEntityParams params)
      throws NotFoundException, ForbiddenException;

  /**
   * Get the tracked entity matching given {@code UID} under the privileges of the currently
   * authenticated user. If {@code program} is defined, program attributes for such program are
   * included, otherwise only TETAs are included. It will include enrollments, relationships,
   * attributes and ownerships as defined in {@code params}.
   *
   * @deprecated use {@link #getNewTrackedEntity(UID, UID, TrackedEntityParams)} instead.
   */
  @Deprecated(forRemoval = true)
  @Nonnull
  TrackedEntity getTrackedEntity(@Nonnull UID uid, UID program, @Nonnull TrackedEntityParams params)
      throws NotFoundException, ForbiddenException, BadRequestException;

  /** Get all tracked entities matching given params. */
  @Nonnull
  List<TrackedEntity> getTrackedEntities(TrackedEntityOperationParams operationParams)
      throws BadRequestException, ForbiddenException, NotFoundException;

  /** Get a page of tracked entities matching given params. */
  @Nonnull
  Page<TrackedEntity> getTrackedEntities(TrackedEntityOperationParams params, PageParams pageParams)
      throws BadRequestException, ForbiddenException, NotFoundException;

  /**
   * Fields the {@link #getTrackedEntities(TrackedEntityOperationParams)} can order tracked entities
   * by. Ordering by fields other than these is considered a programmer error. Validation of user
   * provided field names should occur before calling {@link
   * #getTrackedEntities(TrackedEntityOperationParams)}.
   */
  Set<String> getOrderableFields();
}
