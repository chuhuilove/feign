package feign.assertj;

import org.assertj.core.api.Assertions;
import feign.RequestTemplate;

public class FeignAssertions extends Assertions {

  public static RequestTemplateAssert assertThat(RequestTemplate actual) {
    return new RequestTemplateAssert(actual);
  }
}
