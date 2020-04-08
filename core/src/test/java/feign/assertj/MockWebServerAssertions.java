package feign.assertj;

import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;

public class MockWebServerAssertions extends Assertions {

  public static RecordedRequestAssert assertThat(RecordedRequest actual) {
    return new RecordedRequestAssert(actual);
  }
}
