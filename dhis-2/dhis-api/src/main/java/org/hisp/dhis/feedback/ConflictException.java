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

import static org.hisp.dhis.common.OpenApi.Response.Status.CONFLICT;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hisp.dhis.common.OpenApi;
import org.hisp.dhis.webmessage.WebResponse;

@Getter
@Accessors(chain = true)
@OpenApi.Response(status = CONFLICT, value = WebResponse.class)
@SuppressWarnings({"java:S1165", "java:S1948"})
public final class ConflictException extends Exception implements Error {
  public static <E extends RuntimeException, V> V on(Class<E> type, Supplier<V> operation)
      throws ConflictException {
    return Error.rethrow(type, ConflictException::new, operation);
  }

  public static <E extends RuntimeException, V> V on(
      Class<E> type, Function<E, ConflictException> map, Supplier<V> operation)
      throws ConflictException {
    return Error.rethrowMapped(type, map, operation);
  }

  private final ErrorCode code;
  private final Object[] args;

  @Setter private String devMessage;

  @Setter private ObjectReport objectReport;

  @Setter private MergeReport mergeReport;

  public ConflictException(String message) {
    this(message, null);
  }

  public ConflictException(String message, String devMessage) {
    super(message);
    this.devMessage = devMessage;
    this.code = ErrorCode.E1004;
    this.args = new Object[0];
  }

  public ConflictException(ErrorCode code, Object... args) {
    super(MessageFormat.format(code.getMessage(), args));
    this.code = code;
    this.args = args;
  }

  public ConflictException(ErrorMessage message) {
    super(message.getMessage());
    this.code = message.getErrorCode();
    List<String> args = message.getArgs();
    this.args = args == null ? new Object[0] : args.toArray();
  }
}
