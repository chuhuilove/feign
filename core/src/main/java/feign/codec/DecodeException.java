package feign.codec;

import feign.FeignException;
import feign.Request;
import static feign.Util.checkNotNull;

/**
 * Similar to {@code javax.websocket.DecodeException}, raised when a problem occurs decoding a
 * message. Note that {@code DecodeException} is not an {@code IOException}, nor does it have one
 * set as its cause.
 */
public class DecodeException extends FeignException {

  private static final long serialVersionUID = 1L;

  /**
   * @param message the reason for the failure.
   */
  public DecodeException(int status, String message, Request request) {
    super(status, checkNotNull(message, "message"), request);
  }

  /**
   * @param message possibly null reason for the failure.
   * @param cause the cause of the error.
   */
  public DecodeException(int status, String message, Request request, Throwable cause) {
    super(status, message, request, checkNotNull(cause, "cause"));
  }
}
