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
package org.hisp.dhis.program.notification;

import java.util.List;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Zubair Asghar
 */
@RequiredArgsConstructor
@Service("org.hisp.dhis.program.notification.ProgramNotificationInstanceService")
public class DefaultProgramNotificationInstanceService
    implements ProgramNotificationInstanceService {
  private final ProgramNotificationInstanceStore notificationInstanceStore;

  @Nonnull
  @Override
  @Transactional(readOnly = true)
  public List<ProgramNotificationInstance> getProgramNotificationInstances(
      ProgramNotificationInstanceParam programNotificationInstanceParam) {
    return notificationInstanceStore.getProgramNotificationInstances(
        programNotificationInstanceParam);
  }

  @Nonnull
  @Override
  @Transactional(readOnly = true)
  public List<ProgramNotificationInstance> getProgramNotificationInstancesPage(
      ProgramNotificationInstanceParam programNotificationInstanceParam) {
    return notificationInstanceStore.getProgramNotificationInstancesPage(
        programNotificationInstanceParam);
  }

  @Override
  public Long countProgramNotificationInstances(ProgramNotificationInstanceParam params) {
    return notificationInstanceStore.countProgramNotificationInstances(params);
  }

  @Override
  @Transactional(readOnly = true)
  public ProgramNotificationInstance get(long programNotificationInstance) {
    return notificationInstanceStore.get(programNotificationInstance);
  }

  @Override
  @Transactional
  public void save(ProgramNotificationInstance programNotificationInstance) {
    notificationInstanceStore.save(programNotificationInstance);
  }

  @Override
  @Transactional
  public void update(ProgramNotificationInstance programNotificationInstance) {
    notificationInstanceStore.update(programNotificationInstance);
  }

  @Override
  @Transactional
  public void delete(ProgramNotificationInstance programNotificationInstance) {
    notificationInstanceStore.delete(programNotificationInstance);
  }
}
