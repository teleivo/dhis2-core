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
package org.hisp.dhis.artemis;

import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.hisp.dhis.render.RenderService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Component
public class MessageManager {
  private final JmsTemplate jmsTopicTemplate;

  private final JmsTemplate jmsQueueTemplate;

  private final RenderService renderService;

  public MessageManager(
      @Qualifier("jmsTopicTemplate") JmsTemplate jmsTopicTemplate,
      @Qualifier("jmsQueueTemplate") JmsTemplate jmsQueueTemplate,
      RenderService renderService) {
    this.jmsTopicTemplate = jmsTopicTemplate;
    this.jmsQueueTemplate = jmsQueueTemplate;
    this.renderService = renderService;
  }

  public void send(String destinationName, Message message) {
    jmsTopicTemplate.send(
        destinationName,
        session -> session.createTextMessage(renderService.toJsonAsString(message)));
  }

  public void sendTopic(String destinationName, Message message) {
    jmsTopicTemplate.send(
        new ActiveMQTopic(destinationName),
        session -> session.createTextMessage(renderService.toJsonAsString(message)));
  }

  public void sendQueue(String destinationName, Message message) {
    jmsQueueTemplate.send(
        new ActiveMQQueue(destinationName),
        session -> session.createTextMessage(renderService.toJsonAsString(message)));
  }
}
