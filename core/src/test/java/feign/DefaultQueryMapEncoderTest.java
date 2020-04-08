package feign;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultQueryMapEncoderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private final QueryMapEncoder encoder = new QueryMapEncoder.Default();

  @Test
  public void testEncodesObject_visibleFields() {
    Map<String, Object> expected = new HashMap<>();
    expected.put("foo", "fooz");
    expected.put("bar", "barz");
    expected.put("baz", "bazz");
    VisibleFieldsObject object = new VisibleFieldsObject();
    object.foo = "fooz";
    object.bar = "barz";
    object.baz = "bazz";

    Map<String, Object> encodedMap = encoder.encode(object);
    assertEquals("Unexpected encoded query map", expected, encodedMap);
  }

  @Test
  public void testEncodesObject_visibleFields_emptyObject() {
    VisibleFieldsObject object = new VisibleFieldsObject();
    Map<String, Object> encodedMap = encoder.encode(object);
    assertTrue("Non-empty map generated from null fields: " + encodedMap, encodedMap.isEmpty());
  }

  @Test
  public void testEncodesObject_nonVisibleFields() {
    Map<String, Object> expected = new HashMap<>();
    expected.put("foo", "fooz");
    expected.put("bar", "barz");
    QueryMapEncoderObject object = new QueryMapEncoderObject("fooz", "barz");

    Map<String, Object> encodedMap = encoder.encode(object);
    assertEquals("Unexpected encoded query map", expected, encodedMap);
  }

  @Test
  public void testEncodesObject_nonVisibleFields_emptyObject() {
    QueryMapEncoderObject object = new QueryMapEncoderObject(null, null);
    Map<String, Object> encodedMap = encoder.encode(object);
    assertTrue("Non-empty map generated from null fields", encodedMap.isEmpty());
  }

  static class VisibleFieldsObject {
    String foo;
    String bar;
    String baz;
  }
}

