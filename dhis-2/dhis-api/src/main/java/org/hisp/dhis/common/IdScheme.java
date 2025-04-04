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
package org.hisp.dhis.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.attribute.Attribute;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@ToString
@EqualsAndHashCode
@Getter
@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
@OpenApi.Property(value = IdentifiableProperty.class)
public class IdScheme implements Serializable {

  public static final IdScheme NULL = new IdScheme(null);

  public static final IdScheme ID = new IdScheme(IdentifiableProperty.ID);

  public static final IdScheme UID = new IdScheme(IdentifiableProperty.UID);

  public static final IdScheme UUID = new IdScheme(IdentifiableProperty.UUID);

  public static final IdScheme CODE = new IdScheme(IdentifiableProperty.CODE);

  public static final IdScheme NAME = new IdScheme(IdentifiableProperty.NAME);

  public static final ImmutableMap<IdentifiableProperty, IdScheme> IDPROPERTY_IDSCHEME_MAP =
      ImmutableMap.<IdentifiableProperty, IdScheme>builder()
          .put(IdentifiableProperty.ID, IdScheme.ID)
          .put(IdentifiableProperty.UID, IdScheme.UID)
          .put(IdentifiableProperty.UUID, IdScheme.UUID)
          .put(IdentifiableProperty.CODE, IdScheme.CODE)
          .put(IdentifiableProperty.NAME, IdScheme.NAME)
          .build();

  public static final String ATTR_ID_SCHEME_PREFIX = "ATTRIBUTE:";

  @JsonProperty("type")
  private final IdentifiableProperty identifiableProperty;

  @JsonProperty private String attribute;

  public static IdScheme from(IdScheme idScheme) {
    if (idScheme == null) {
      return IdScheme.NULL;
    }

    return idScheme;
  }

  public static IdScheme from(String scheme) {
    if (scheme == null) {
      return IdScheme.NULL;
    }

    if (IdScheme.isAttribute(scheme)) {
      return new IdScheme(IdentifiableProperty.ATTRIBUTE, scheme.substring(10));
    }

    return IdScheme.from(IdentifiableProperty.valueOf(scheme.toUpperCase()));
  }

  public static IdScheme from(IdentifiableProperty property) {
    if (property == null) {
      return IdScheme.NULL;
    }

    return IDPROPERTY_IDSCHEME_MAP.containsKey(property)
        ? IDPROPERTY_IDSCHEME_MAP.get(property)
        : new IdScheme(property);
  }

  public static IdScheme from(Attribute attribute) {
    return new IdScheme(IdentifiableProperty.ATTRIBUTE, attribute.getUid());
  }

  private IdScheme(IdentifiableProperty identifiableProperty) {
    this.identifiableProperty = identifiableProperty;
  }

  @JsonCreator
  public IdScheme(
      @JsonProperty("type") IdentifiableProperty identifiableProperty,
      @JsonProperty("attribute") String attribute) {
    this.identifiableProperty = identifiableProperty;
    this.attribute = attribute;
  }

  public String getIdentifiableString() {
    return identifiableProperty != null ? identifiableProperty.toString() : null;
  }

  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  public boolean is(IdentifiableProperty identifiableProperty) {
    return this.identifiableProperty == identifiableProperty;
  }

  public boolean isNull() {
    return null == this.identifiableProperty;
  }

  public boolean isNotNull() {
    return !isNull();
  }

  public boolean isAttribute() {
    return IdentifiableProperty.ATTRIBUTE == identifiableProperty
        && !StringUtils.isEmpty(attribute);
  }

  /**
   * Returns a canonical name representation of this ID scheme.
   *
   * @return a canonical name representation of this ID scheme.
   */
  public String name() {
    if (IdentifiableProperty.ATTRIBUTE == identifiableProperty && attribute != null) {
      return ATTR_ID_SCHEME_PREFIX + attribute;
    } else {
      return identifiableProperty.name();
    }
  }

  public static boolean isAttribute(String str) {
    return !StringUtils.isEmpty(str)
        && str.toUpperCase().startsWith(ATTR_ID_SCHEME_PREFIX)
        && str.length() == 21;
  }
}
