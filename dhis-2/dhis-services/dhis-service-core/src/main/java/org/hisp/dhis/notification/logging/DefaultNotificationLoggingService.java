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
package org.hisp.dhis.notification.logging;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Created by zubair@dhis2.org on 10.01.18. */
@RequiredArgsConstructor
@Service("org.hisp.dhis.notification.logging.NotificationLoggingService")
public class DefaultNotificationLoggingService implements NotificationLoggingService {
  private final NotificationLoggingStore notificationLoggingStore;

  @Override
  @Transactional(readOnly = true)
  public ExternalNotificationLogEntry get(String uid) {
    return notificationLoggingStore.getByUid(uid);
  }

  @Override
  @Transactional(readOnly = true)
  public ExternalNotificationLogEntry get(int id) {
    return notificationLoggingStore.get(id);
  }

  @Override
  @Transactional(readOnly = true)
  public ExternalNotificationLogEntry getByKey(String key) {
    return notificationLoggingStore.getByKey(key);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ExternalNotificationLogEntry> getAllLogEntries() {
    return notificationLoggingStore.getAll();
  }

  @Override
  @Transactional(readOnly = true)
  public ExternalNotificationLogEntry getByTemplateUid(String templateUid) {
    return notificationLoggingStore.getByTemplateUid(templateUid);
  }

  @Override
  @Transactional
  public void save(ExternalNotificationLogEntry entry) {
    notificationLoggingStore.save(entry);
  }

  @Override
  @Transactional
  public void update(ExternalNotificationLogEntry entry) {
    notificationLoggingStore.update(entry);
  }
}
