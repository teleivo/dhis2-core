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
package org.hisp.dhis.webapi.controller.tracker.export.trigramsummary;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;

/**
 * TrigramSummary object to store trigram indexing status
 *
 * @author Ameen Mohamed
 */
@JacksonXmlRootElement
@NoArgsConstructor
public class TrigramSummary {
  private List<ObjectNode> indexedAttributes = new ArrayList<>();

  private List<ObjectNode> indexableAttributes = new ArrayList<>();

  private List<ObjectNode> obsoleteIndexedAttributes = new ArrayList<>();

  @JsonProperty
  public List<ObjectNode> getIndexedAttributes() {
    return indexedAttributes;
  }

  public void setIndexedAttributes(List<ObjectNode> indexedAttributes) {
    this.indexedAttributes = indexedAttributes;
  }

  @JsonProperty
  public List<ObjectNode> getIndexableAttributes() {
    return indexableAttributes;
  }

  public void setIndexableAttributes(List<ObjectNode> indexableAttributes) {
    this.indexableAttributes = indexableAttributes;
  }

  @JsonProperty
  public List<ObjectNode> getObsoleteIndexedAttributes() {
    return obsoleteIndexedAttributes;
  }

  public void setObsoleteIndexedAttributes(List<ObjectNode> obsoleteIndexedAttributes) {
    this.obsoleteIndexedAttributes = obsoleteIndexedAttributes;
  }
}
