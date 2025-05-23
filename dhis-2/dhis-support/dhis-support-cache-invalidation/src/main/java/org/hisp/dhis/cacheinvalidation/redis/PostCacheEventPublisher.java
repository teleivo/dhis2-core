/*
 * Copyright (c) 2004-2023, University of Oslo
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
package org.hisp.dhis.cacheinvalidation.redis;

import static org.hisp.dhis.cacheinvalidation.redis.CacheInvalidationConfig.CHANNEL_NAME;
import static org.hisp.dhis.cacheinvalidation.redis.CacheInvalidationConfig.EXCLUDE_LIST;

import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dataset.CompleteDataSetRegistration;
import org.hisp.dhis.datastatistics.DataStatisticsEvent;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.hibernate.HibernateProxyUtils;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * It listens for events from Hibernate and publishes a message to Redis when an event occurs
 *
 * @author Morten Svanæs <msvanaes@dhis2.org>
 */
@Slf4j
@Component
@Conditional(value = CacheInvalidationEnabledCondition.class)
public class PostCacheEventPublisher
    implements PostCommitUpdateEventListener,
        PostCommitInsertEventListener,
        PostCommitDeleteEventListener {

  @Autowired private CacheInvalidationMessagePublisher messagePublisher;

  @Autowired
  @Qualifier("cacheInvalidationServerId")
  private String serverInstanceId;

  public CacheInvalidationMessagePublisher getMessagePublisher() {
    return messagePublisher;
  }

  @Override
  public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
    handleMessage(CacheEventOperation.UPDATE, postUpdateEvent.getEntity(), postUpdateEvent.getId());
  }

  @Override
  public void onPostInsert(PostInsertEvent postInsertEvent) {
    handleMessage(CacheEventOperation.INSERT, postInsertEvent.getEntity(), postInsertEvent.getId());
  }

  @Override
  public void onPostDelete(PostDeleteEvent postDeleteEvent) {
    handleMessage(CacheEventOperation.DELETE, postDeleteEvent.getEntity(), postDeleteEvent.getId());
  }

  private void handleMessage(CacheEventOperation operation, Object entity, Serializable id) {
    Class<?> realClass = HibernateProxyUtils.getRealClass(entity);

    if (entity instanceof DataValue) {
      id = getDataValueId(entity);
    } else if (entity instanceof TrackedEntityAttributeValue) {
      id = getTrackedEntityAttributeValueId(entity);
    } else if (entity instanceof CompleteDataSetRegistration) {
      id = getCompleteDataSetRegistrationId(entity);
    } else if (entity instanceof DataStatisticsEvent) {
      DataStatisticsEvent dataStatisticsEvent = (DataStatisticsEvent) entity;
      id = dataStatisticsEvent.getId();
    } else if (entity instanceof IdentifiableObject) {
      IdentifiableObject identifiableObject = (IdentifiableObject) entity;
      id = identifiableObject.getId();
    }

    String op = operation.name().toLowerCase();
    String message = serverInstanceId + ":" + op + ":" + realClass.getName() + ":" + id;

    publishMessage(realClass, message);
  }

  private void publishMessage(Class<?> realClass, String message) {
    if (!EXCLUDE_LIST.contains(realClass)) {
      messagePublisher.publish(CHANNEL_NAME, message);
    } else {
      log.debug("Ignoring excluded class: " + realClass.getName());
    }
  }

  private Serializable getDataValueId(Object entity) {
    DataValue dataValue = (DataValue) entity;

    long dataElementId = dataValue.getDataElement().getId();
    long periodId = dataValue.getPeriod().getId();
    long organisationUnitId = dataValue.getSource().getId();
    long categoryOptionComboId = dataValue.getAttributeOptionCombo().getId();
    long attributeOptionComboId = dataValue.getAttributeOptionCombo().getId();

    return dataElementId
        + ";"
        + periodId
        + ";"
        + organisationUnitId
        + ";"
        + categoryOptionComboId
        + ";"
        + attributeOptionComboId;
  }

  private Serializable getTrackedEntityAttributeValueId(Object entity) {
    TrackedEntityAttributeValue trackedEntityAttributeValue = (TrackedEntityAttributeValue) entity;

    long trackedEntityAttributeId = trackedEntityAttributeValue.getAttribute().getId();
    long trackedEntityId = trackedEntityAttributeValue.getTrackedEntity().getId();

    return trackedEntityAttributeId + ";" + trackedEntityId;
  }

  private Serializable getCompleteDataSetRegistrationId(Object entity) {
    CompleteDataSetRegistration completeDataSetRegistration = (CompleteDataSetRegistration) entity;

    long dataSetId = completeDataSetRegistration.getDataSet().getId();
    long periodId = completeDataSetRegistration.getPeriod().getId();
    long organisationUnitId = completeDataSetRegistration.getSource().getId();
    long categoryOptionComboId = completeDataSetRegistration.getAttributeOptionCombo().getId();

    return dataSetId + ";" + periodId + ";" + organisationUnitId + ";" + categoryOptionComboId;
  }

  @Override
  public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
    return true;
  }

  @Override
  public boolean requiresPostCommitHandling(EntityPersister persister) {
    return PostCommitUpdateEventListener.super.requiresPostCommitHandling(persister);
  }

  @Override
  public void onPostUpdateCommitFailed(PostUpdateEvent event) {
    log.debug("onPostUpdateCommitFailed: " + event);
  }

  @Override
  public void onPostInsertCommitFailed(PostInsertEvent event) {
    log.debug("onPostInsertCommitFailed: " + event);
  }

  @Override
  public void onPostDeleteCommitFailed(PostDeleteEvent event) {
    log.debug("onPostDeleteCommitFailed: " + event);
  }
}
