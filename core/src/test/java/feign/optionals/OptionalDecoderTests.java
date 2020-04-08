package feign.optionals;

import feign.Feign;
import feign.RequestLine;
import feign.codec.Decoder;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Test;
import java.io.IOException;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

public class OptionalDecoderTests {

  interface OptionalInterface {
    @RequestLine("GET /")
    Optional<String> getAsOptional();

    @RequestLine("GET /")
    String get();
  }

  @Test
  public void simple404OptionalTest() throws IOException, InterruptedException {
    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setResponseCode(404));
    server.enqueue(new MockResponse().setBody("foo"));

    final OptionalInterface api = Feign.builder()
        .decode404()
        .decoder(new OptionalDecoder(new Decoder.Default()))
        .target(OptionalInterface.class, server.url("/").toString());

    assertThat(api.getAsOptional().isPresent()).isFalse();
    assertThat(api.getAsOptional().get()).isEqualTo("foo");
  }

  @Test
  public void simple204OptionalTest() throws IOException, InterruptedException {
    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setResponseCode(204));

    final OptionalInterface api = Feign.builder()
        .decoder(new OptionalDecoder(new Decoder.Default()))
        .target(OptionalInterface.class, server.url("/").toString());

    assertThat(api.getAsOptional().isPresent()).isFalse();
  }

  @Test
  public void test200WithOptionalString() throws IOException, InterruptedException {
    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setResponseCode(200).setBody("foo"));

    final OptionalInterface api = Feign.builder()
        .decoder(new OptionalDecoder(new Decoder.Default()))
        .target(OptionalInterface.class, server.url("/").toString());

    Optional<String> response = api.getAsOptional();

    assertThat(response.isPresent()).isTrue();
    assertThat(response).isEqualTo(Optional.of("foo"));
  }

  @Test
  public void test200WhenResponseBodyIsNull() throws IOException, InterruptedException {
    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setResponseCode(200));

    final OptionalInterface api = Feign.builder()
        .decoder(new OptionalDecoder(((response, type) -> null)))
        .target(OptionalInterface.class, server.url("/").toString());

    assertThat(api.getAsOptional().isPresent()).isFalse();
  }

  @Test
  public void test200WhenDecodingNoOptional() throws IOException, InterruptedException {
    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setResponseCode(200).setBody("foo"));

    final OptionalInterface api = Feign.builder()
        .decoder(new OptionalDecoder(new Decoder.Default()))
        .target(OptionalInterface.class, server.url("/").toString());

    assertThat(api.get()).isEqualTo("foo");
  }
}
