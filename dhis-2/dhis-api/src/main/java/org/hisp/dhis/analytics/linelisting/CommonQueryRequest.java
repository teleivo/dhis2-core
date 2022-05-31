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
package org.hisp.dhis.analytics.linelisting;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import org.hisp.dhis.common.AnalyticsPagingCriteria;
import org.hisp.dhis.common.IdScheme;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;

@Getter
@Setter
public class CommonQueryRequest extends AnalyticsPagingCriteria
{

    private String userOrgUnit;

    private Set<String> dimension = new HashSet<>();

    private Set<String> filter = new HashSet<>();

    private Set<String> headers = new HashSet<>();

    private OrganisationUnitSelectionMode ouMode;

    private Set<String> asc = new HashSet<>();

    private Set<String> desc = new HashSet<>();

    private boolean skipMeta;

    private boolean skipData;

    private IdScheme dataIdScheme = IdScheme.UID;

    private boolean totalPages;

    private Date relativePeriodDate;

}
