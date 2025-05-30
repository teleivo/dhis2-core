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
package org.hisp.dhis.hibernate;

/**
 * Interface which contains methods for generating predicates which are used validating sharing
 * access permission.
 *
 * @author Lars Helge Overland
 */
public interface SharingHibernateGenericStore<T> extends InternalHibernateGenericStore<T> {
  /**
   * Get List of JPA Query Predicates for checking AclService.LIKE_READ_METADATA sharing access of
   * {@link UserDetails}.
   *
   * @param builder {@link CriteriaBuilder} used for generating {@link Predicate}
   * @return List of {@link Predicate}
   */
  //  List<Function<Root<T>, Predicate>> getSharingPredicates(CriteriaBuilder builder);
  //
  //  /**
  //   * Get List of JPA Query Predicates for checking AclService.LIKE_READ_METADATA sharing access
  // of
  //   * current {@link UserDetails}.
  //   *
  //   * @param builder {@link CriteriaBuilder} used for generating {@link Predicate}
  //   * @return List of {@link Predicate}
  //   */
  //  List<Function<Root<T>, Predicate>> getSharingPredicates(
  //      CriteriaBuilder builder, UserDetails user);
  //
  //  /**
  //   * Get List of JPA Query Predicates for checking sharing access of current {@link UserDetails}
  //   * based on given access String.
  //   *
  //   * @param builder {@link CriteriaBuilder} used for generating {@link Predicate}.
  //   * @param user {@link User} for checking.
  //   * @param access access string for checking.
  //   * @return List of {@link Predicate}
  //   */
  //  List<Function<Root<T>, Predicate>> getSharingPredicatesXX(CriteriaBuilder builder, String
  // access);
  //
  //  /**
  //   * Get List of JPA Query Predicates for checking sharing access of current {@link UserDetails}
  //   * based on given access String.
  //   *
  //   * @param builder {@link CriteriaBuilder} used for generating {@link Predicate}
  //   * @param access access string for checking.
  //   * @return List of {@link Predicate}
  //   */
  //  List<Function<Root<T>, Predicate>> getSharingPredicates(CriteriaBuilder builder, String
  // access);

  /**
   * Get List of JPA Query Predicates for checking AclService.LIKE_DATA_READ data sharing access of
   * current {@link UserDetails}.
   *
   * @param builder {@link CriteriaBuilder} used for generating {@link Predicate}
   * @return List of {@link Predicate}
   */
  //  List<Function<Root<T>, Predicate>> getDataSharingPredicates(
  //      CriteriaBuilder builder, UserDetails user);
  //
  //  /**
  //   * Get List of JPA Query Predicates for checking data sharing access of current {@link
  //   * UserDetails} based on given access String.
  //   *
  //   * @param builder {@link CriteriaBuilder} used for generating {@link Predicate}.
  //   * @param user {@link User} for checking.
  //   * @param access access string for checking.
  //   * @return List of {@link Predicate}
  //   */
  //  List<Function<Root<T>, Predicate>> getDataSharingPredicates(
  //      CriteriaBuilder builder, UserDetails user, String access);
}
