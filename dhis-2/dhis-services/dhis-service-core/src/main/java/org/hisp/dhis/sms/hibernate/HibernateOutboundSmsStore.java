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
package org.hisp.dhis.sms.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import java.util.Date;
import java.util.List;
import org.hisp.dhis.common.hibernate.HibernateIdentifiableObjectStore;
import org.hisp.dhis.hibernate.JpaQueryParameters;
import org.hisp.dhis.security.acl.AclService;
import org.hisp.dhis.sms.outbound.OutboundSms;
import org.hisp.dhis.sms.outbound.OutboundSmsStatus;
import org.hisp.dhis.sms.outbound.OutboundSmsStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository("org.hisp.dhis.sms.hibernate.OutboundSmsStore")
public class HibernateOutboundSmsStore extends HibernateIdentifiableObjectStore<OutboundSms>
    implements OutboundSmsStore {
  public HibernateOutboundSmsStore(
      EntityManager entityManager,
      JdbcTemplate jdbcTemplate,
      ApplicationEventPublisher publisher,
      AclService aclService) {
    super(entityManager, jdbcTemplate, publisher, OutboundSms.class, aclService, true);
  }

  // -------------------------------------------------------------------------
  // Implementation
  // -------------------------------------------------------------------------

  @Override
  public void saveOutboundSms(OutboundSms sms) {
    checkDate(sms);
    save(sms);
  }

  private void checkDate(OutboundSms sms) {
    if (sms.getDate() == null) {
      sms.setDate(new Date());
    }
  }

  @Override
  public List<OutboundSms> get(OutboundSmsStatus status) {
    CriteriaBuilder builder = getCriteriaBuilder();

    JpaQueryParameters<OutboundSms> parameters =
        new JpaQueryParameters<OutboundSms>().addOrder(root -> builder.desc(root.get("date")));

    if (status != null) {
      parameters.addPredicate(root -> builder.equal(root.get("status"), status));
    }

    return getList(builder, parameters);
  }

  @Override
  public List<OutboundSms> get(
      OutboundSmsStatus status, Integer min, Integer max, boolean hasPagination) {
    CriteriaBuilder builder = getCriteriaBuilder();

    JpaQueryParameters<OutboundSms> parameters =
        new JpaQueryParameters<OutboundSms>().addOrder(root -> builder.desc(root.get("date")));

    if (status != null) {
      parameters.addPredicate(root -> builder.equal(root.get("status"), status));
    }

    if (hasPagination) {
      parameters.setFirstResult(min).setMaxResults(max);
    }

    return getList(builder, parameters);
  }

  @Override
  public List<OutboundSms> getAllOutboundSms(Integer min, Integer max, boolean hasPagination) {
    CriteriaBuilder builder = getCriteriaBuilder();

    JpaQueryParameters<OutboundSms> parameters =
        new JpaQueryParameters<OutboundSms>().addOrder(root -> builder.desc(root.get("date")));

    if (hasPagination) {
      parameters.setFirstResult(min).setMaxResults(max);
    }

    return getList(builder, parameters);
  }
}
