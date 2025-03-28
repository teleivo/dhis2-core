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
package org.hisp.dhis.trackedentity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import org.hisp.dhis.test.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@TestInstance(Lifecycle.PER_CLASS)
@Transactional
class TrackedEntityTypeAttributeStoreTest extends PostgresIntegrationTestBase {

  @Autowired private TrackedEntityTypeStore entityTypeStore;

  @Autowired private TrackedEntityAttributeStore attributeStore;

  @Autowired private TrackedEntityTypeAttributeStore entityTypeAttributeStore;

  @Test
  void testGetAttributesByTrackedEntityTypes() {
    TrackedEntityType entityTypeA = createTrackedEntityType('A');
    TrackedEntityType entityTypeB = createTrackedEntityType('B');
    TrackedEntityType entityTypeC = createTrackedEntityType('C');
    entityTypeStore.save(entityTypeA);
    entityTypeStore.save(entityTypeB);
    entityTypeStore.save(entityTypeC);
    TrackedEntityAttribute attributeA = createTrackedEntityAttribute('A');
    attributeStore.save(attributeA);
    TrackedEntityTypeAttribute typeAttributeA = new TrackedEntityTypeAttribute();
    typeAttributeA.setTrackedEntityType(entityTypeA);
    typeAttributeA.setTrackedEntityAttribute(attributeA);
    TrackedEntityTypeAttribute typeAttributeB = new TrackedEntityTypeAttribute();
    typeAttributeB.setTrackedEntityType(entityTypeB);
    typeAttributeB.setTrackedEntityAttribute(attributeA);
    TrackedEntityTypeAttribute typeAttributeC = new TrackedEntityTypeAttribute();
    typeAttributeC.setTrackedEntityType(entityTypeA);
    typeAttributeC.setTrackedEntityAttribute(attributeA);
    entityTypeAttributeStore.save(typeAttributeA);
    entityTypeAttributeStore.save(typeAttributeB);
    entityTypeAttributeStore.save(typeAttributeC);
    assertEquals(1, entityTypeAttributeStore.getAttributes(Lists.newArrayList(entityTypeA)).size());
    assertEquals(1, entityTypeAttributeStore.getAttributes(Lists.newArrayList(entityTypeB)).size());
    assertEquals(0, entityTypeAttributeStore.getAttributes(Lists.newArrayList(entityTypeC)).size());
  }
}
