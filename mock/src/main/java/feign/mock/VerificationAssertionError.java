package feign.mock;

public class VerificationAssertionError extends AssertionError {

  private static final long serialVersionUID = -3302777023656958993L;

  public VerificationAssertionError(String message, Object... arguments) {
    super(String.format(message, arguments));
  }

}
