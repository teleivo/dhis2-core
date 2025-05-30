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
package org.hisp.dhis.webapi.utils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static org.hisp.dhis.common.cache.CacheStrategy.RESPECT_SYSTEM_SETTING;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.common.HashUtils;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectUtils;
import org.hisp.dhis.common.cache.CacheStrategy;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserDetails;
import org.hisp.dhis.util.DateUtils;
import org.hisp.dhis.webapi.service.WebCache;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

/**
 * @author Lars Helge Overland
 */
@Component
public class ContextUtils {
  public static final String CONTENT_TYPE_PDF = "application/pdf";

  public static final String CONTENT_TYPE_JSON_ZIP = "application/json+zip";
  public static final String CONTENT_TYPE_JSON_GZIP = "application/json+gzip";

  public static final String CONTENT_TYPE_GZIP = "application/gzip";

  public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

  public static final String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";

  public static final String CONTENT_TYPE_TEXT = "text/plain; charset=UTF-8";

  public static final String CONTENT_TYPE_CSS = "text/css; charset=UTF-8";

  public static final String CONTENT_TYPE_XML = "application/xml; charset=UTF-8";

  public static final String CONTENT_TYPE_XML_ADX = "application/adx+xml; charset=UTF-8";

  public static final String CONTENT_TYPE_CSV = "application/csv; charset=UTF-8";

  public static final String CONTENT_TYPE_TEXT_CSV = "text/csv";

  public static final String CONTENT_TYPE_CSV_GZIP = "application/csv+gzip";

  public static final String CONTENT_TYPE_CSV_ZIP = "application/csv+zip";

  public static final String CONTENT_TYPE_PNG = "image/png";

  public static final String CONTENT_TYPE_EXCEL = "application/vnd.ms-excel";

  public static final String CONTENT_TYPE_JAVASCRIPT = "application/javascript; charset=UTF-8";

  public static final String CONTENT_TYPE_FORM_ENCODED = "application/x-www-form-urlencoded";

  public static final String HEADER_USER_AGENT = "User-Agent";

  public static final String HEADER_CACHE_CONTROL = "Cache-Control";

  public static final String HEADER_LOCATION = "Location";

  public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

  public static final String HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";

  public static final String BINARY_HEADER_CONTENT_TRANSFER_ENCODING = "binary";

  public static final String HEADER_VALUE_NO_STORE =
      "no-cache, no-store, max-age=0, must-revalidate";

  public static final String HEADER_IF_NONE_MATCH = "If-None-Match";

  public static final String HEADER_ETAG = "ETag";

  private static final String QUOTE = "\"";

  private static final String QUERY_STRING_SEP = "?";

  /**
   * Regular expression that extracts the attachment file name from a content disposition header
   * value.
   */
  private static final Pattern CONTENT_DISPOSITION_ATTACHMENT_FILENAME_PATTERN =
      Pattern.compile("attachment;\\s*filename=\"?([^;\"]+)\"?");

  private final WebCache webCache;

  public ContextUtils(WebCache webCache) {
    checkNotNull(webCache);

    this.webCache = webCache;
  }

  public void configureResponse(
      HttpServletResponse response, String contentType, CacheStrategy cacheStrategy) {
    configureResponse(response, contentType, cacheStrategy, null, false);
  }

  public void configureAnalyticsResponse(
      HttpServletResponse response,
      String contentType,
      CacheStrategy cacheStrategy,
      String filename,
      boolean attachment,
      Date latestEndDate) {
    // Progressive cache will always take priority
    if (RESPECT_SYSTEM_SETTING == cacheStrategy
        && webCache.isProgressiveCachingEnabled()
        && latestEndDate != null) {
      // Uses the progressive TTL
      final CacheControl cacheControl = webCache.getCacheControlFor(latestEndDate);

      configureResponse(response, contentType, filename, attachment, cacheControl);
    } else {
      // Respects the fixed (predefined) settings
      configureResponse(response, contentType, cacheStrategy, filename, attachment);
    }
  }

  public void configureResponse(
      HttpServletResponse response,
      String contentType,
      CacheStrategy cacheStrategy,
      String filename,
      boolean attachment) {
    CacheControl cacheControl = webCache.getCacheControlFor(cacheStrategy);

    configureResponse(response, contentType, filename, attachment, cacheControl);
  }

  private void configureResponse(
      HttpServletResponse response,
      String contentType,
      String filename,
      boolean attachment,
      CacheControl cacheControl) {
    if (contentType != null) {
      response.setContentType(contentType);
    }

    response.setHeader(HEADER_CACHE_CONTROL, cacheControl.getHeaderValue());

    if (filename != null) {
      final String type = attachment ? "attachment" : "inline";

      response.setHeader(HEADER_CONTENT_DISPOSITION, type + "; filename=\"" + filename + "\"");
    }
  }

  public static HttpHeaders noCacheNoStoreMustRevalidate() {
    HttpHeaders headers = new HttpHeaders();
    headers.setCacheControl("no-cache, no-store, must-revalidate");
    return headers;
  }

  public static HttpServletResponse setCacheControl(
      HttpServletResponse response, CacheControl value) {
    response.setHeader(HEADER_CACHE_CONTROL, value.getHeaderValue());
    return response;
  }

  public static HttpServletResponse setNoStore(HttpServletResponse response) {
    response.setHeader(HEADER_CACHE_CONTROL, HEADER_VALUE_NO_STORE);
    return response;
  }

  @CheckForNull
  public static HttpServletRequest getRequest() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    return attributes != null ? attributes.getRequest() : null;
  }

  public static String getRootPath(HttpServletRequest request) {
    return HttpServletRequestPaths.getContextPath(request) + request.getServletPath();
  }

  /**
   * Omits the default ports.
   *
   * @return The full URL as requested by the client or null of no request context is available
   */
  @CheckForNull
  public static String getRequestURL() {
    HttpServletRequest request = getRequest();
    if (request == null) return null;
    String scheme = request.getScheme();
    String domain = request.getServerName();
    String port = ":" + request.getServerPort();
    if ((":80".equals(port) && "http".equals(scheme))
        || (":443".equals(port) && "https".equals(scheme))) port = "";
    String path = request.getRequestURI();
    String query = "?" + nullToEmpty(request.getQueryString());
    if ("?".equals(query)) query = "";
    scheme += "://";
    return scheme + domain + port + path + query;
  }

  /**
   * This method looks up the ETag sent in the request from the "If-None-Match" header value,
   * generates an ETag based on the given collection of IdentifiableObjects and compares them for
   * equality. If this evaluates to true, it will set status code 304 Not Modified on the response
   * and remove all elements from the given list. It will set the ETag header on the response.
   *
   * @param request the {@link HttpServletRequest}.
   * @param response the {@link HttpServletResponse}.
   * @return true if the eTag values are equals, false otherwise.
   */
  public static boolean isNotModified(
      HttpServletRequest request,
      HttpServletResponse response,
      Collection<? extends IdentifiableObject> objects) {
    String tag = quote(IdentifiableObjectUtils.getLastUpdatedTag(objects));

    response.setHeader(HEADER_ETAG, tag);

    String inputTag = request.getHeader(HEADER_IF_NONE_MATCH);

    if (objects != null && inputTag != null && inputTag.equals(tag)) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

      return true;
    }

    return false;
  }

  /**
   * This method looks up the ETag sent in the request from the "If-None-Match" header value and
   * compares it to the given tag. If they match, it will set status code 304 Not Modified on the
   * response. It will set the ETag header on the response. It will wrap the given tag in quotes.
   *
   * @param request the {@link HttpServletRequest}.
   * @param response the {@link HttpServletResponse}.
   * @param tag the tag to compare.
   * @return true if the given tag match the request tag and the response is considered not
   *     modified, false if not.
   */
  public static boolean isNotModified(
      HttpServletRequest request, HttpServletResponse response, String tag) {
    tag = tag != null ? quote(tag) : null;

    String inputTag = request.getHeader(HEADER_IF_NONE_MATCH);

    response.setHeader(HEADER_ETAG, tag);

    if (inputTag != null && inputTag.equals(tag)) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

      return true;
    }

    return false;
  }

  /**
   * Returns an ETag based on the given last modified date and user, returned as 32 character string
   * representation of an MD5 hash. The user is helpful to make the ETag unique for responses where
   * sharing is applied.
   *
   * @param lastModified the last modified {@link Date}.
   * @param user the {@link User}.
   * @return an ETag string.
   */
  public static String getEtag(Date lastModified, UserDetails user) {
    if (lastModified == null || user == null) {
      return null;
    }

    String value = String.format("%s-%s", DateUtils.toLongDate(lastModified), user.getUid());

    return HashUtils.hashMD5(value.getBytes());
  }

  /**
   * Indicates whether the given requests indicates that it accepts a compressed response.
   *
   * @param request the HttpServletRequest.
   * @return whether the given requests indicates that it accepts a compressed response.
   */
  public static boolean isAcceptCsvGzip(HttpServletRequest request) {
    return request != null
        && ((request.getPathInfo() != null && request.getPathInfo().endsWith(".gz"))
            || (request.getHeader(HttpHeaders.ACCEPT) != null
                && request.getHeader(HttpHeaders.ACCEPT).contains("application/csv+gzip")));
  }

  /**
   * Indicates whether the given requests indicates that it accepts a compressed response.
   *
   * @param request the HttpServletRequest.
   * @return whether the given requests indicates that it accepts a compressed response.
   */
  public static boolean isAcceptCsvZip(HttpServletRequest request) {
    return request != null
        && ((request.getPathInfo() != null && request.getPathInfo().endsWith(".zip"))
            || (request.getHeader(HttpHeaders.ACCEPT) != null
                && request.getHeader(HttpHeaders.ACCEPT).contains(CONTENT_TYPE_CSV_ZIP)));
  }

  /**
   * Extracts and returns the file name from a content disposition header value.
   *
   * @param contentDispositionHeaderValue the content disposition header value from which the file
   *     name should be extracted.
   * @return the extracted file name or <code>null</code> content disposition has no filename.
   */
  public static String getAttachmentFileName(String contentDispositionHeaderValue) {
    if (contentDispositionHeaderValue == null) {
      return null;
    }

    Matcher matcher =
        CONTENT_DISPOSITION_ATTACHMENT_FILENAME_PATTERN.matcher(contentDispositionHeaderValue);
    return matcher.matches() ? matcher.group(1) : null;
  }

  /**
   * Returns the value associated with a double wildcard ({@code **}) in the request mapping.
   *
   * <p>As an example, for a request mapping {@code /apps/**} and a request {@code
   * /apps/data-visualizer/index.html?id=123}, this method will return {@code
   * data-visualizer/index.html?id=123}.
   *
   * @param request the {@link HttpServletRequest}.
   * @return the wildcard value.
   */
  public static String getWildcardPathValue(HttpServletRequest request) {
    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    String bestMatchPattern =
        (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    String wildcardPath = new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);

    String queryString =
        !StringUtils.isBlank(request.getQueryString())
            ? QUERY_STRING_SEP.concat(request.getQueryString())
            : StringUtils.EMPTY;

    return String.format("%s%s", wildcardPath, queryString);
  }

  /**
   * Removes ".format.compression" extension if present, for example "data.xml.zip" will be replaced
   * with "data".
   *
   * <p>We do this to make sure the filename is without any additional extensions in case the client
   * mistakenly also sends in the extension they want.
   *
   * @param name String to string
   * @param format Format to match for
   * @param compression Compression to match for
   * @return String without .format.compression extension
   */
  public static String stripFormatCompressionExtension(
      String name, String format, String compression) {
    return name != null ? name.replace("." + format + "." + compression, "") : "";
  }

  /**
   * Quotes the given strings using double quotes.
   *
   * @param string the string.
   * @return a quoted value.
   */
  static String quote(String string) {
    return String.format("%s%s%s", QUOTE, string, QUOTE);
  }
}
