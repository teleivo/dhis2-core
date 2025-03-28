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
package org.hisp.dhis.relationship;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.MetadataObject;
import org.hisp.dhis.schema.PropertyType;
import org.hisp.dhis.schema.annotation.Property;
import org.hisp.dhis.translation.Translatable;

/**
 * @author Abyot Asalefew
 * @author Stian Sandvold
 */
@JacksonXmlRootElement(localName = "relationshipType", namespace = DxfNamespaces.DXF_2_0)
public class RelationshipType extends BaseIdentifiableObject implements MetadataObject {
  private RelationshipConstraint fromConstraint;

  private RelationshipConstraint toConstraint;

  private String description;

  private boolean bidirectional = false;

  private String fromToName;

  private String toFromName;

  private boolean referral;

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  public RelationshipType() {}

  // -------------------------------------------------------------------------
  // Getters and setters
  // -------------------------------------------------------------------------

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  @Property(value = PropertyType.COMPLEX, required = Property.Value.TRUE)
  public RelationshipConstraint getFromConstraint() {
    return fromConstraint;
  }

  public void setFromConstraint(RelationshipConstraint fromConstraint) {
    this.fromConstraint = fromConstraint;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  @Property(value = PropertyType.COMPLEX, required = Property.Value.TRUE)
  public RelationshipConstraint getToConstraint() {
    return toConstraint;
  }

  public void setToConstraint(RelationshipConstraint toConstraint) {
    this.toConstraint = toConstraint;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public boolean isBidirectional() {
    return bidirectional;
  }

  public void setBidirectional(boolean bidirectional) {
    this.bidirectional = bidirectional;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public String getFromToName() {
    return fromToName;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  @Translatable(propertyName = "fromToName", key = "RELATIONSHIP_FROM_TO_NAME")
  public String getDisplayFromToName() {
    return getTranslation("RELATIONSHIP_FROM_TO_NAME", getFromToName());
  }

  public void setFromToName(String fromToName) {
    this.fromToName = fromToName;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public String getToFromName() {
    return toFromName;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  @Translatable(propertyName = "toFromName", key = "RELATIONSHIP_TO_FROM_NAME")
  public String getDisplayToFromName() {
    return getTranslation("RELATIONSHIP_TO_FROM_NAME", getToFromName());
  }

  public void setToFromName(String toFromName) {
    this.toFromName = toFromName;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public boolean isReferral() {
    return referral;
  }

  public void setReferral(boolean referral) {
    this.referral = referral;
  }
}
