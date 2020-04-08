package feign.http2client.test;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import java.io.IOException;
import feign.*;
import feign.client.AbstractClientTest;
import feign.http2client.Http2Client;
import okhttp3.mockwebserver.MockResponse;

/**
 * Tests client-specific behavior, such as ensuring Content-Length is sent when specified.
 */
@Ignore
public class Http2ClientTest extends AbstractClientTest {

  public interface TestInterface {
    @RequestLine("PATCH /patch")
    @Headers({"Accept: text/plain"})
    String patch(String var1);

    @RequestLine("PATCH /patch")
    @Headers({"Accept: text/plain"})
    String patch();
  }

  @Override
  @Test
  public void testPatch() throws Exception {
    final TestInterface api =
        newBuilder().target(TestInterface.class, "https://nghttp2.org/httpbin/");
    Assertions.assertThat(api.patch(""))
        .contains("https://nghttp2.org/httpbin/patch");
  }

  @Override
  @Test
  public void noResponseBodyForPatch() {
    final TestInterface api =
        newBuilder().target(TestInterface.class, "https://nghttp2.org/httpbin/");
    Assertions.assertThat(api.patch())
        .contains("https://nghttp2.org/httpbin/patch");
  }

  @Override
  @Test
  public void reasonPhraseIsOptional() throws IOException, InterruptedException {
    server.enqueue(new MockResponse()
        .addHeader("Reason-Phrase", "There is A reason")
        .setStatus("HTTP/1.1 " + 200));

    final AbstractClientTest.TestInterface api = newBuilder()
        .target(AbstractClientTest.TestInterface.class, "http://localhost:" + server.getPort());

    final Response response = api.post("foo");

    assertThat(response.status()).isEqualTo(200);
    assertThat(response.reason()).isEqualTo("There is A reason");
  }


  @Override
  @Test
  public void testVeryLongResponseNullLength() {
    // client is too smart to fall for a body that is 8 bytes long
  }

  @Override
  public Feign.Builder newBuilder() {
    return Feign.builder().client(new Http2Client());
  }

}
