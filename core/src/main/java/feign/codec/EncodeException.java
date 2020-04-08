package feign.codec;

import static feign.Util.checkNotNull;
import feign.FeignException;

/**
 * Similar to {@code javax.websocket.EncodeException}, raised when a problem occurs encoding a
 * message. Note that {@code EncodeException} is not an {@code IOException}, nor does it have one
 * set as its cause.
 */
public class EncodeException extends FeignException {

  private static final long serialVersionUID = 1L;

  /**
   * @param message the reason for the failure.
   */
  public EncodeException(String message) {
    super(-1, checkNotNull(message, "message"));
  }

  /**
   * @param message possibly null reason for the failure.
   * @param cause the cause of the error.
   */
  public EncodeException(String message, Throwable cause) {
    super(-1, message, checkNotNull(cause, "cause"));
  }
}
