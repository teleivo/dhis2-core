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
package org.hisp.dhis.datastore;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hisp.dhis.datastore.DatastoreQuery.Filter;
import org.hisp.dhis.feedback.ConflictException;
import org.hisp.dhis.feedback.ErrorCode;

/**
 * Contains the {@link DatastoreQuery} semantic validation.
 *
 * @author Jan Bernitt
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatastoreQueryValidator {
  public static void validate(DatastoreQuery query) throws ConflictException {
    for (Filter f : query.getFilters()) {
      boolean isUnary = f.getOperator().isUnary();
      if (f.isKeyPath() && isUnary) {
        throw filterException(f, "key filters cannot be used with unary operators");
      }
      if (!isUnary && f.getValue().isBlank()) {
        throw filterException(f, "the operator `" + f.getOperator() + "` requires a value");
      }
      if (isUnary && !f.getValue().isBlank()) {
        throw filterException(
            f, "the operator `" + f.getOperator() + "` is unary and does not require a value");
      }
    }
  }

  private static ConflictException filterException(Filter f, String msg) {
    return new ConflictException(ErrorCode.E7653, f.toString(), msg);
  }
}
