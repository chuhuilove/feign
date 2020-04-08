package feign;

import static feign.assertj.MockWebServerAssertions.assertThat;
import feign.Target.HardCodedTarget;
import java.net.URI;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Rule;
import org.junit.Test;

public class TargetTest {

  @Rule
  public final MockWebServer server = new MockWebServer();

  interface TestQuery {

    @RequestLine("GET /{path}?query={query}")
    Response get(@Param("path") String path, @Param("query") String query);
  }

  @Test
  public void baseCaseQueryParamsArePercentEncoded() throws InterruptedException {
    server.enqueue(new MockResponse());

    String baseUrl = server.url("/default").toString();

    Feign.builder().target(TestQuery.class, baseUrl).get("slash/foo", "slash/bar");

    assertThat(server.takeRequest()).hasPath("/default/slash/foo?query=slash%2Fbar");
  }

  /**
   * Per <a href="https://github.com/Netflix/feign/issues/227">#227</a>, some may want to opt out of
   * percent encoding. Here's how.
   */
  @Test
  public void targetCanCreateCustomRequest() throws InterruptedException {
    server.enqueue(new MockResponse());

    String baseUrl = server.url("/default").toString();
    Target<TestQuery> custom = new HardCodedTarget<TestQuery>(TestQuery.class, baseUrl) {

      @Override
      public Request apply(RequestTemplate input) {
        Request urlEncoded = super.apply(input);
        return Request.create(
            urlEncoded.httpMethod(),
            urlEncoded.url().replace("%2F", "/"),
            urlEncoded.headers(),
            urlEncoded.requestBody().asBytes(), urlEncoded.charset());
      }
    };

    Feign.builder().target(custom).get("slash/foo", "slash/bar");

    assertThat(server.takeRequest()).hasPath("/default/slash/foo?query=slash/bar");
  }

  interface UriTarget {

    @RequestLine("GET")
    Response get(URI uri);
  }

  @Test
  public void emptyTarget() throws InterruptedException {
    server.enqueue(new MockResponse());

    UriTarget uriTarget = Feign.builder()
        .target(Target.EmptyTarget.create(UriTarget.class));

    String host = server.getHostName();
    int port = server.getPort();

    uriTarget.get(URI.create("http://" + host + ":" + port + "/path?query=param"));

    assertThat(server.takeRequest()).hasPath("/path?query=param").hasQueryParams("query=param");
  }

  @Test
  public void hardCodedTargetWithURI() throws InterruptedException {
    server.enqueue(new MockResponse());

    String host = server.getHostName();
    int port = server.getPort();
    String base = "http://" + host + ":" + port;

    UriTarget uriTarget = Feign.builder()
        .target(UriTarget.class, base);

    uriTarget.get(URI.create("http://" + host + ":" + port + "/path?query=param"));

    assertThat(server.takeRequest()).hasPath("/path?query=param").hasQueryParams("query=param");
  }
}
