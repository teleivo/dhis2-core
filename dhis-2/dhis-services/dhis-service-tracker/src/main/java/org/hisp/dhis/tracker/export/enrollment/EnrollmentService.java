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
package org.hisp.dhis.tracker.export.enrollment;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.feedback.BadRequestException;
import org.hisp.dhis.feedback.ForbiddenException;
import org.hisp.dhis.feedback.NotFoundException;
import org.hisp.dhis.program.Enrollment;
import org.hisp.dhis.tracker.Page;
import org.hisp.dhis.tracker.PageParams;

public interface EnrollmentService {
  @Nonnull
  Enrollment getEnrollment(UID uid) throws ForbiddenException, NotFoundException;

  @Nonnull
  Enrollment getEnrollment(UID uid, EnrollmentParams params)
      throws NotFoundException, ForbiddenException;

  /** Get all enrollments matching given params. */
  @Nonnull
  List<Enrollment> getEnrollments(EnrollmentOperationParams params)
      throws BadRequestException, ForbiddenException;

  /** Get a page of enrollments matching given params. */
  @Nonnull
  Page<Enrollment> getEnrollments(EnrollmentOperationParams params, PageParams pageParams)
      throws BadRequestException, ForbiddenException;

  /**
   * Get event matching given {@code UID} under the privileges the user in the context. This method
   * does not get the events relationships.
   */
  @Nonnull
  List<Enrollment> getEnrollments(@Nonnull Set<UID> uids) throws ForbiddenException;

  /**
   * Fields the {@link #getEnrollments(EnrollmentOperationParams)} can order enrollments by.
   * Ordering by fields other than these is considered a programmer error. Validation of user
   * provided field names should occur before calling {@link
   * #getEnrollments(EnrollmentOperationParams)}.
   */
  Set<String> getOrderableFields();
}
