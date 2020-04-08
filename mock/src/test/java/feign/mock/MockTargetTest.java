package feign.mock;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class MockTargetTest {

  private MockTarget<MockTargetTest> target;

  @Before
  public void setup() {
    target = new MockTarget<>(MockTargetTest.class);
  }

  @Test
  public void test() {
    assertThat(target.name(), equalTo("MockTargetTest"));
  }

}
