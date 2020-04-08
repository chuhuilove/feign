package feign;

import org.junit.Test;

public class FeignExceptionTest {

  @Test(expected = NullPointerException.class)
  public void nullRequestShouldThrowNPEwThrowable() {
    new Derived(404, "message", null, new Throwable());
  }

  @Test(expected = NullPointerException.class)
  public void nullRequestShouldThrowNPEwThrowableAndBytes() {
    new Derived(404, "message", null, new Throwable(), new byte[1]);
  }

  @Test(expected = NullPointerException.class)
  public void nullRequestShouldThrowNPE() {
    new Derived(404, "message", null);
  }

  @Test(expected = NullPointerException.class)
  public void nullRequestShouldThrowNPEwBytes() {
    new Derived(404, "message", null, new byte[1]);
  }

  static class Derived extends FeignException {

    public Derived(int status, String message, Request request, Throwable cause) {
      super(status, message, request, cause);
    }

    public Derived(int status, String message, Request request, Throwable cause, byte[] content) {
      super(status, message, request, cause, content);
    }

    public Derived(int status, String message, Request request) {
      super(status, message, request);
    }

    public Derived(int status, String message, Request request, byte[] content) {
      super(status, message, request, content);
    }
  }

}
