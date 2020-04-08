package feign.querymap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.HashMap;
import java.util.Map;
import feign.QueryMapEncoder;

/**
 * Test for {@link FieldQueryMapEncoder}
 */
public class FieldQueryMapEncoderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private final QueryMapEncoder encoder = new FieldQueryMapEncoder();

  @Test
  public void testDefaultEncoder_normalClassWithValues() {
    final Map<String, Object> expected = new HashMap<>();
    expected.put("foo", "fooz");
    expected.put("bar", "barz");
    final NormalObject normalObject = new NormalObject("fooz", "barz");

    final Map<String, Object> encodedMap = encoder.encode(normalObject);

    assertEquals("Unexpected encoded query map", expected, encodedMap);
  }

  @Test
  public void testDefaultEncoder_normalClassWithOutValues() {
    final NormalObject normalObject = new NormalObject(null, null);

    final Map<String, Object> encodedMap = encoder.encode(normalObject);

    assertTrue("Non-empty map generated from null getter: " + encodedMap, encodedMap.isEmpty());
  }

  class NormalObject {

    private NormalObject(String foo, String bar) {
      this.foo = foo;
      this.bar = bar;
    }

    private final String foo;
    private final String bar;
  }

}
