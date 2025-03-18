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
package org.hisp.dhis.webapi.controller.tracker.export.enrollment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hisp.dhis.common.OpenApi;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.fieldfiltering.FieldFilterParser;
import org.hisp.dhis.fieldfiltering.FieldPath;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Enrollment;
import org.hisp.dhis.program.EnrollmentStatus;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityType;
import org.hisp.dhis.webapi.controller.event.webrequest.OrderCriteria;
import org.hisp.dhis.webapi.controller.tracker.FieldsRequestParam;
import org.hisp.dhis.webapi.controller.tracker.PageRequestParams;
import org.hisp.dhis.webapi.webdomain.EndDateTime;
import org.hisp.dhis.webapi.webdomain.StartDateTime;

/** Represents query parameters sent to {@link EnrollmentsExportController}. */
@OpenApi.Shared(name = "EnrollmentRequestParams")
@OpenApi.Property
@Data
@NoArgsConstructor
public class EnrollmentRequestParams implements PageRequestParams, FieldsRequestParam {
  static final String DEFAULT_FIELDS_PARAM = "*,!relationships,!events,!attributes";

  @OpenApi.Description(
      """
Get the given page.
""")
  @OpenApi.Property(defaultValue = "1")
  private Integer page;

  @OpenApi.Description(
      """
Get given number of items per page.
""")
  @OpenApi.Property(defaultValue = "50")
  private Integer pageSize;

  @OpenApi.Description(
      """
Get the total number of items and pages in the pager.

**Only enable this if absolutely necessary as this is resource intensive.** Use the pagers
`prev/nextPage` to determine if there is a previous or a next page instead.
""")
  private boolean totalPages = false;

  @OpenApi.Description(
      """
Get all items by specifying `paging=false`. Requests are paginated by default.

**Be aware that the performance is directly related to the amount of data requested. Larger pages
will take more time to return.**
""")
  private boolean paging = true;

  private List<OrderCriteria> order = new ArrayList<>();

  @OpenApi.Property({UID[].class, OrganisationUnit.class})
  private Set<UID> orgUnits = new HashSet<>();

  private OrganisationUnitSelectionMode orgUnitMode;

  @OpenApi.Property({UID.class, Program.class})
  private UID program;

  /**
   * @deprecated use {@link #status} instead
   */
  @Deprecated(since = "2.42")
  private EnrollmentStatus programStatus;

  private EnrollmentStatus status;

  private Boolean followUp;

  private StartDateTime updatedAfter;

  private String updatedWithin;

  private StartDateTime enrolledAfter;

  private EndDateTime enrolledBefore;

  @OpenApi.Property({UID.class, TrackedEntityType.class})
  private UID trackedEntityType;

  @OpenApi.Property({UID.class, TrackedEntity.class})
  private UID trackedEntity;

  @OpenApi.Property({UID[].class, Enrollment.class})
  private Set<UID> enrollments = new HashSet<>();

  private boolean includeDeleted = false;

  @OpenApi.Property(value = String[].class)
  private List<FieldPath> fields = FieldFilterParser.parse(DEFAULT_FIELDS_PARAM);
}
