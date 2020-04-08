package feign;

import feign.Request.HttpMethod;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.net.URI;
import feign.Target.EmptyTarget;
import static feign.assertj.FeignAssertions.assertThat;

public class EmptyTargetTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void whenNameNotSupplied() {
    assertThat(EmptyTarget.create(UriInterface.class))
        .isEqualTo(EmptyTarget.create(UriInterface.class, "empty:UriInterface"));
  }

  @Test
  public void toString_withoutName() {
    assertThat(EmptyTarget.create(UriInterface.class).toString())
        .isEqualTo("EmptyTarget(type=UriInterface)");
  }

  @Test
  public void toString_withName() {
    assertThat(EmptyTarget.create(UriInterface.class, "manager-access").toString())
        .isEqualTo("EmptyTarget(type=UriInterface, name=manager-access)");
  }

  @Test
  public void mustApplyToAbsoluteUrl() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("Request with non-absolute URL not supported with empty target");

    EmptyTarget.create(UriInterface.class)
        .apply(new RequestTemplate().method(HttpMethod.GET).uri("/relative"));
  }

  interface UriInterface {

    @RequestLine("GET /")
    Response get(URI endpoint);
  }
}
