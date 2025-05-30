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
package org.hisp.dhis.appmanager;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.io.Serializable;
import org.hisp.dhis.common.DxfNamespaces;

/**
 * @author Saptarshi
 */
@JacksonXmlRootElement(localName = "appIcons", namespace = DxfNamespaces.DXF_2_0)
public class AppIcons implements Serializable {
  /** Determines if a de-serialized file is compatible with this class. */
  private static final long serialVersionUID = 5041924160867190242L;

  /** Optional. */
  @JsonProperty("16")
  @JacksonXmlProperty(localName = "icon_16", namespace = DxfNamespaces.DXF_2_0)
  private String icon16;

  @JsonProperty("48")
  @JacksonXmlProperty(localName = "icon_48", namespace = DxfNamespaces.DXF_2_0)
  private String icon48;

  @JsonProperty("128")
  @JacksonXmlProperty(localName = "icon_128", namespace = DxfNamespaces.DXF_2_0)
  private String icon128;

  public String getIcon16() {
    return icon16;
  }

  public void setIcon16(String icon16) {
    this.icon16 = icon16;
  }

  public String getIcon48() {
    return icon48;
  }

  public void setIcon48(String icon48) {
    this.icon48 = icon48;
  }

  public String getIcon128() {
    return icon128;
  }

  public void setIcon128(String icon128) {
    this.icon128 = icon128;
  }
}
