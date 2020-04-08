package feign;

import feign.Request.HttpMethod;
import java.util.Date;

/**
 * This exception is raised when the {@link Response} is deemed to be retryable, typically via an
 * {@link feign.codec.ErrorDecoder} when the {@link Response#status() status} is 503.
 */
public class RetryableException extends FeignException {

  private static final long serialVersionUID = 1L;

  private final Long retryAfter;
  private final HttpMethod httpMethod;

  /**
   * @param retryAfter usually corresponds to the {@link feign.Util#RETRY_AFTER} header.
   */
  public RetryableException(int status, String message, HttpMethod httpMethod, Throwable cause,
      Date retryAfter, Request request) {
    super(status, message, request, cause);
    this.httpMethod = httpMethod;
    this.retryAfter = retryAfter != null ? retryAfter.getTime() : null;
  }

  /**
   * @param retryAfter usually corresponds to the {@link feign.Util#RETRY_AFTER} header.
   */
  public RetryableException(int status, String message, HttpMethod httpMethod, Date retryAfter,
      Request request) {
    super(status, message, request);
    this.httpMethod = httpMethod;
    this.retryAfter = retryAfter != null ? retryAfter.getTime() : null;
  }

  /**
   * Sometimes corresponds to the {@link feign.Util#RETRY_AFTER} header present in {@code 503}
   * status. Other times parsed from an application-specific response. Null if unknown.
   */
  public Date retryAfter() {
    return retryAfter != null ? new Date(retryAfter) : null;
  }

  public HttpMethod method() {
    return this.httpMethod;
  }
}
