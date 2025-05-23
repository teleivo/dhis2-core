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
package org.hisp.dhis.node.config;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class Config {
  /**
   * Inclusion strategy to use. There are a few already defined inclusions in the Inclusions enum.
   *
   * @see org.hisp.dhis.node.config.InclusionStrategy.Include
   */
  private InclusionStrategy inclusionStrategy = InclusionStrategy.Include.NON_NULL;

  /**
   * Property map that can hold any key=value pair, can be used to set custom properties that only
   * certain serializers know about.
   */
  private final Map<String, Object> properties = Maps.newHashMap();

  public Config() {}

  public InclusionStrategy getInclusionStrategy() {
    return inclusionStrategy;
  }

  public void setInclusionStrategy(InclusionStrategy inclusionStrategy) {
    this.inclusionStrategy = inclusionStrategy;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Config config = (Config) o;

    final boolean propertiesAreEqual =
        Arrays.deepEquals(
            properties.entrySet().toArray(), ((Config) o).getProperties().entrySet().toArray());

    return Objects.equals(inclusionStrategy, config.inclusionStrategy) && propertiesAreEqual;
  }

  @Override
  public int hashCode() {
    return Objects.hash(inclusionStrategy, properties);
  }
}
