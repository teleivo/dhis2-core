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
package org.hisp.dhis.analytics.event.data;

import static org.hisp.dhis.analytics.common.DimensionsServiceCommon.OperationType.AGGREGATE;
import static org.hisp.dhis.analytics.common.DimensionsServiceCommon.OperationType.QUERY;
import static org.hisp.dhis.analytics.common.DimensionsServiceCommon.collectDimensions;
import static org.hisp.dhis.analytics.common.DimensionsServiceCommon.filterByValueType;
import static org.hisp.dhis.common.PrefixedDimensions.ofItemsWithProgram;
import static org.hisp.dhis.common.PrefixedDimensions.ofProgramStageDataElements;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hisp.dhis.analytics.common.DimensionsServiceCommon;
import org.hisp.dhis.analytics.event.EnrollmentAnalyticsDimensionsService;
import org.hisp.dhis.common.PrefixedDimension;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.security.acl.AclService;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.user.CurrentUserUtil;
import org.hisp.dhis.user.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultEnrollmentAnalyticsDimensionsService
    implements EnrollmentAnalyticsDimensionsService {
  private final ProgramService programService;

  private final AclService aclService;

  @Override
  public List<PrefixedDimension> getQueryDimensionsByProgramId(String programId) {

    UserDetails currentUserDetails = CurrentUserUtil.getCurrentUserDetails();
    return Optional.of(programId)
        .map(programService::getProgram)
        .filter(Program::isRegistration)
        .map(
            program ->
                collectDimensions(
                    List.of(
                        ofItemsWithProgram(
                            program,
                            program.getProgramIndicators().stream()
                                .filter(pi -> aclService.canRead(currentUserDetails, pi))
                                .collect(Collectors.toSet())),
                        getProgramStageDataElements(QUERY, program),
                        filterByValueType(
                            QUERY,
                            ofItemsWithProgram(
                                program, getTeasIfRegistrationAndNotConfidential(program))))))
        .orElse(List.of());
  }

  private Collection<PrefixedDimension> getProgramStageDataElements(
      DimensionsServiceCommon.OperationType operationType, Program program) {
    return program.getProgramStages().stream()
        .map(ProgramStage::getProgramStageDataElements)
        .map(
            programStageDataElements ->
                filterByValueType(
                    operationType, ofProgramStageDataElements(programStageDataElements)))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  @Override
  public List<PrefixedDimension> getAggregateDimensionsByProgramStageId(String programId) {
    return Optional.of(programId)
        .map(programService::getProgram)
        .map(
            program ->
                collectDimensions(
                    List.of(
                        getProgramStageDataElements(AGGREGATE, program),
                        filterByValueType(
                            AGGREGATE,
                            ofItemsWithProgram(program, program.getTrackedEntityAttributes())))))
        .orElse(List.of());
  }

  private Collection<TrackedEntityAttribute> getTeasIfRegistrationAndNotConfidential(
      Program program) {
    return Optional.of(program)
        .filter(Program::isRegistration)
        .map(Program::getTrackedEntityAttributes)
        .orElse(List.of())
        .stream()
        .filter(this::isNotConfidential)
        .collect(Collectors.toList());
  }

  private boolean isNotConfidential(TrackedEntityAttribute trackedEntityAttribute) {
    return !trackedEntityAttribute.isConfidentialBool();
  }
}
