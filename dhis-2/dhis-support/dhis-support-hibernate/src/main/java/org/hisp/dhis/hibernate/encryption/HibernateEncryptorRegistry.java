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
package org.hisp.dhis.hibernate.encryption;

import com.google.common.collect.Maps;
import java.util.Map;
import org.jasypt.encryption.pbe.PBEStringEncryptor;

/**
 * Singleton registry for all (named) Hibernate Encryptors. {@link
 * org.hisp.dhis.hibernate.encryption.type.EncryptedStringUserType EncryptedStringUserType} depends
 * on this singleton to access the appropriate encryptor(s).
 *
 * @author Halvdan Hoem Grelland
 */
public final class HibernateEncryptorRegistry {
  private static final HibernateEncryptorRegistry INSTANCE = new HibernateEncryptorRegistry();

  private final Map<String, PBEStringEncryptor> encryptors = Maps.newHashMap();

  private HibernateEncryptorRegistry() {}

  /**
   * Returns the (singleton) instance of the registry.
   *
   * @return this registry.
   */
  public static HibernateEncryptorRegistry getInstance() {
    return INSTANCE;
  }

  /**
   * Registers the given {@link PBEStringEncryptor PBEStringEncryptors} by name.
   *
   * @param encryptors a map of names and encryptors.
   */
  public synchronized void setEncryptors(Map<String, PBEStringEncryptor> encryptors) {
    INSTANCE.encryptors.putAll(encryptors);
  }

  /**
   * Get encryptor from registry by name.
   *
   * @param name the name of the encryptor.
   * @return an instance of {@link PBEStringEncryptor} or null.
   */
  public PBEStringEncryptor getEncryptor(String name) {
    return INSTANCE.encryptors.get(name);
  }
}
