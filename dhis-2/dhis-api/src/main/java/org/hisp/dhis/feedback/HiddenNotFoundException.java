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
package org.hisp.dhis.feedback;

import static org.hisp.dhis.common.OpenApi.Response.Status.OK;

import java.text.MessageFormat;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.hisp.dhis.common.OpenApi;
import org.hisp.dhis.webmessage.WebResponse;

@Getter
@Accessors(chain = true)
@OpenApi.Response(status = OK, value = WebResponse.class)
public final class HiddenNotFoundException extends Exception implements Error {
  public static <E extends RuntimeException, V> V on(Class<E> type, Supplier<V> operation)
      throws HiddenNotFoundException {
    return Error.rethrow(type, HiddenNotFoundException::new, operation);
  }

  public static <E extends RuntimeException, V> V on(
      Class<E> type, Function<E, HiddenNotFoundException> map, Supplier<V> operation)
      throws HiddenNotFoundException {
    return Error.rethrowMapped(type, map, operation);
  }

  private final ErrorCode code;

  public HiddenNotFoundException(Class<?> type, String uid) {
    this(type.getSimpleName() + " with id " + uid + " could not be found.");
  }

  public HiddenNotFoundException(String message) {
    super(message);
    this.code = ErrorCode.E1005;
  }

  public HiddenNotFoundException(ErrorCode code, Object... args) {
    super(MessageFormat.format(code.getMessage(), args));
    this.code = code;
  }
}
