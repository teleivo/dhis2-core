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
package org.hisp.dhis.userdatastore;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.hisp.dhis.common.IllegalQueryException;
import org.hisp.dhis.datastore.DatastoreFields;
import org.hisp.dhis.datastore.DatastoreQuery;
import org.hisp.dhis.feedback.BadRequestException;
import org.hisp.dhis.feedback.ConflictException;
import org.hisp.dhis.user.User;

/**
 * @author Stian Sandvold
 */
public interface UserDatastoreService {

  boolean isUsedNamespace(User user, String namespace);

  /**
   * Retrieves a KeyJsonValue based on a user and key
   *
   * @param user the user where the key is associated
   * @param namespace the namespace associated with the key
   * @param key the key referencing the value @return the UserKeyJsonValue matching the key and
   *     namespace
   */
  UserDatastoreEntry getUserEntry(User user, String namespace, String key);

  /**
   * Adds a new UserKeyJsonValue
   *
   * @param entry the UserKeyJsonValue to be stored
   */
  void addEntry(UserDatastoreEntry entry) throws ConflictException, BadRequestException;

  /**
   * Updates the entry value (path is undefined or empty) or updates the existing value the the *
   * provided path with the provided value.
   *
   * <p>If a roll size is provided and the exiting value (at path) is an array the array is not *
   * replaced with the value but the value is appended to the array. The head of the array is *
   * dropped if the size of the array is equal or larger than the roll size.
   *
   * @param ns namespace to update
   * @param key key to update
   * @param value the new JSON value, null to remove the entry or clear the property at the provided
   *     path
   * @param path to update, null or empty to update the root (the entire value)
   * @param roll when set the value is appended to arrays instead of replacing them while also *
   *     rolling (dropping the array head element when its size
   */
  void updateEntry(
      @Nonnull String ns,
      @Nonnull String key,
      @CheckForNull String value,
      @CheckForNull String path,
      @CheckForNull Integer roll)
      throws BadRequestException;

  /**
   * Deletes a UserKeyJsonValue
   *
   * @param entry the UserKeyJsonValue to be deleted.
   */
  void deleteEntry(UserDatastoreEntry entry);

  /**
   * Returns a list of namespaces connected to the given user
   *
   * @param user the user connected to the namespaces
   * @return List of strings representing namespaces or an empty list if no namespaces are found
   */
  List<String> getNamespacesByUser(User user);

  /**
   * Returns a list of keys in the given namespace connected to the given user
   *
   * @param user connected to keys
   * @param namespace to fetch keys from
   * @return a list of keys or an empty list if no keys are found
   */
  List<String> getKeysByUserAndNamespace(User user, String namespace);

  /**
   * Deletes all keys associated with a given user and namespace
   *
   * @param user the user associated with namespace to delete
   * @param namespace the namespace to delete
   */
  void deleteNamespace(User user, String namespace);

  /**
   * Validates and plans a {@link DatastoreQuery}. This might correct or otherwise update the
   * provided query.
   *
   * @param query to validate and plan
   * @throws IllegalQueryException when the query is not valid
   */
  DatastoreQuery plan(DatastoreQuery query) throws ConflictException;

  /**
   * Stream the matching entry fields to a transformer or consumer function.
   *
   * <p>Note that this API cannot return the {@link Stream} since it has to be processed within the
   * transaction bounds of the function call. For the same reason a transformer function has to
   * process the stream in a way that actually will evaluate the stream.
   *
   * @param query query parameters
   * @param transform transformer or consumer for the stream of matches
   * @param <T> type of the transformed stream
   * @return the transformed stream
   */
  <T> T getEntries(User user, DatastoreQuery query, Function<Stream<DatastoreFields>, T> transform)
      throws ConflictException;
}
