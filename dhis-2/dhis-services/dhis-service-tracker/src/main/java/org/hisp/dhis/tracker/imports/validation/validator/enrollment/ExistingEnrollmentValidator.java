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
package org.hisp.dhis.tracker.imports.validation.validator.enrollment;

import static org.hisp.dhis.tracker.imports.validation.ValidationCode.E1015;
import static org.hisp.dhis.tracker.imports.validation.ValidationCode.E1016;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.program.Enrollment;
import org.hisp.dhis.program.EnrollmentStatus;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.tracker.imports.bundle.TrackerBundle;
import org.hisp.dhis.tracker.imports.validation.Reporter;
import org.hisp.dhis.tracker.imports.validation.Validator;

/**
 * @author Morten Svanæs <msvanaes@dhis2.org>
 */
class ExistingEnrollmentValidator
    implements Validator<org.hisp.dhis.tracker.imports.domain.Enrollment> {
  @Override
  public void validate(
      Reporter reporter,
      TrackerBundle bundle,
      org.hisp.dhis.tracker.imports.domain.Enrollment enrollment) {
    if (EnrollmentStatus.CANCELLED == enrollment.getStatus()) {
      return;
    }

    Program program = bundle.getPreheat().getProgram(enrollment.getProgram());

    if ((EnrollmentStatus.COMPLETED == enrollment.getStatus()
        && Boolean.FALSE.equals(program.getOnlyEnrollOnce()))) {
      return;
    }

    validateTeiNotEnrolledAlready(reporter, bundle, enrollment, program);
  }

  private void validateTeiNotEnrolledAlready(
      Reporter reporter,
      TrackerBundle bundle,
      org.hisp.dhis.tracker.imports.domain.Enrollment enrollment,
      Program program) {
    TrackedEntity te = getTrackedEntity(bundle, enrollment.getTrackedEntity());

    Set<org.hisp.dhis.tracker.imports.domain.Enrollment> payloadEnrollment =
        bundle.getEnrollments().stream()
            .filter(Objects::nonNull)
            .filter(e -> e.getProgram().isEqualTo(program))
            .filter(
                e ->
                    e.getTrackedEntity().equals(UID.of(te))
                        && !e.getEnrollment().equals(enrollment.getEnrollment()))
            .filter(
                e ->
                    EnrollmentStatus.ACTIVE == e.getStatus()
                        || EnrollmentStatus.COMPLETED == e.getStatus())
            .collect(Collectors.toSet());

    Set<org.hisp.dhis.tracker.imports.domain.Enrollment> dbEnrollment =
        bundle
            .getPreheat()
            .getTrackedEntityToEnrollmentMap()
            .getOrDefault(enrollment.getTrackedEntity(), new ArrayList<>())
            .stream()
            .filter(Objects::nonNull)
            .filter(
                e ->
                    e.getProgram().getUid().equals(program.getUid())
                        && !e.getUid().equals(enrollment.getEnrollment().getValue()))
            .filter(
                e ->
                    EnrollmentStatus.ACTIVE == e.getStatus()
                        || EnrollmentStatus.COMPLETED == e.getStatus())
            .distinct()
            .map(this::getEnrollmentFromDbEnrollment)
            .collect(Collectors.toSet());

    // Priority to payload
    Collection<org.hisp.dhis.tracker.imports.domain.Enrollment> mergedEnrollments =
        Stream.of(payloadEnrollment, dbEnrollment)
            .flatMap(Set::stream)
            .filter(e -> !Objects.equals(e.getEnrollment(), enrollment.getEnrollment()))
            .collect(
                Collectors.toMap(
                    org.hisp.dhis.tracker.imports.domain.Enrollment::getEnrollment,
                    p -> p,
                    (org.hisp.dhis.tracker.imports.domain.Enrollment x,
                        org.hisp.dhis.tracker.imports.domain.Enrollment y) -> x))
            .values();

    if (EnrollmentStatus.ACTIVE == enrollment.getStatus()) {
      Set<org.hisp.dhis.tracker.imports.domain.Enrollment> activeOnly =
          mergedEnrollments.stream()
              .filter(e -> EnrollmentStatus.ACTIVE == e.getStatus())
              .collect(Collectors.toSet());

      if (!activeOnly.isEmpty()) {
        reporter.addError(enrollment, E1015, te, program);
      }
    }

    if (Boolean.TRUE.equals(program.getOnlyEnrollOnce()) && !mergedEnrollments.isEmpty()) {
      reporter.addError(enrollment, E1016, te, program);
    }
  }

  public org.hisp.dhis.tracker.imports.domain.Enrollment getEnrollmentFromDbEnrollment(
      Enrollment dbEnrollment) {
    org.hisp.dhis.tracker.imports.domain.Enrollment enrollment =
        new org.hisp.dhis.tracker.imports.domain.Enrollment();
    enrollment.setEnrollment(UID.of(dbEnrollment));
    enrollment.setStatus(dbEnrollment.getStatus());

    return enrollment;
  }

  private TrackedEntity getTrackedEntity(TrackerBundle bundle, UID uid) {
    TrackedEntity te = bundle.getPreheat().getTrackedEntity(uid);

    if (te == null && bundle.findTrackedEntityByUid(uid).isPresent()) {
      te = new TrackedEntity();
      te.setUid(uid.getValue());
    }
    return te;
  }
}
