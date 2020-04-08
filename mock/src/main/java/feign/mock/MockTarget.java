package feign.mock;

import feign.Request;
import feign.RequestTemplate;
import feign.Target;

public class MockTarget<E> implements Target<E> {

  private final Class<E> type;

  public MockTarget(Class<E> type) {
    this.type = type;
  }

  @Override
  public Class<E> type() {
    return type;
  }

  @Override
  public String name() {
    return type.getSimpleName();
  }

  @Override
  public String url() {
    return "";
  }

  @Override
  public Request apply(RequestTemplate input) {
    input.target(url());
    return input.request();
  }

}
