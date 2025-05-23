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
package org.hisp.dhis.eventvisualization;

import static org.hisp.dhis.common.DimensionalObjectUtils.asQualifiedDimension;
import static org.hisp.dhis.common.DxfNamespaces.DXF_2_0;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hisp.dhis.common.EmbeddedObject;

/**
 * This object represents an event repetition. It encapsulates all attributes needed by the
 * analytics engine during the query of different events (event repetition).
 *
 * @author maikel arabori
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class EventRepetition implements Serializable, EmbeddedObject {

  /** The attribute which the event repetition belongs to. */
  @JsonProperty
  @JacksonXmlProperty(namespace = DXF_2_0)
  @Nonnull
  private Attribute parent;

  /** The dimension associated with the event repetition. */
  @JsonProperty
  @JacksonXmlProperty(namespace = DXF_2_0)
  @Nonnull
  private String dimension;

  /** The program associated with the event repetition. */
  @JsonProperty
  @JacksonXmlProperty(namespace = DXF_2_0)
  private String program;

  /** The program stage associated with the event repetition. */
  @JsonProperty
  @JacksonXmlProperty(namespace = DXF_2_0)
  private String programStage;

  /**
   * Represents the list of event indexes to be queried. It holds a list of integers that are
   * interpreted as follows:
   *
   * <ul>
   *   <li>1 = First event
   *   <li>2 = Second event
   *   <li>3 = Third event
   *   <li>...
   *   <li>-2 = Third latest event
   *   <li>-1 = Second latest event
   *   <li>0 = Latest event (default)
   * </ul>
   */
  @JsonProperty
  @JacksonXmlProperty(namespace = DXF_2_0)
  @Nonnull
  private List<Integer> indexes = new ArrayList<>();

  public String qualifiedDimension() {
    return asQualifiedDimension(dimension, program, programStage);
  }
}
