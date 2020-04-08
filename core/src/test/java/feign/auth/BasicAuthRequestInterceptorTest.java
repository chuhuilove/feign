package feign.auth;

import org.junit.Test;
import feign.RequestTemplate;
import static feign.assertj.FeignAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.assertj.core.data.MapEntry.entry;

public class BasicAuthRequestInterceptorTest {

  @Test
  public void addsAuthorizationHeader() {
    RequestTemplate template = new RequestTemplate();
    BasicAuthRequestInterceptor interceptor =
        new BasicAuthRequestInterceptor("Aladdin", "open sesame");
    interceptor.apply(template);

    assertThat(template)
        .hasHeaders(
            entry("Authorization", asList("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==")));
  }

  @Test
  public void addsAuthorizationHeader_longUserAndPassword() {
    RequestTemplate template = new RequestTemplate();
    BasicAuthRequestInterceptor interceptor =
        new BasicAuthRequestInterceptor("IOIOIOIOIOIOIOIOIOIOIOIOIOIOIOIOIOIOIO",
            "101010101010101010101010101010101010101010");
    interceptor.apply(template);

    assertThat(template)
        .hasHeaders(
            entry("Authorization", asList(
                "Basic SU9JT0lPSU9JT0lPSU9JT0lPSU9JT0lPSU9JT0lPSU9JT0lPSU86MTAxMDEwMTAxMDEwMTAxMDEwMTAxMDEwMTAxMDEwMTAxMDEwMTAxMDEw")));
  }
}
