package feign.googlehttpclient;

import feign.Feign;
import feign.Feign.Builder;
import feign.client.AbstractClientTest;

public class GoogleHttpClientTest extends AbstractClientTest {
  @Override
  public Builder newBuilder() {
    return Feign.builder()
        .client(new GoogleHttpClient());
  }

  // Google http client doesn't support PATCH. See:
  // https://github.com/googleapis/google-http-java-client/issues/167
  @Override
  public void noResponseBodyForPatch() {}

  @Override
  public void testPatch() {}
}
