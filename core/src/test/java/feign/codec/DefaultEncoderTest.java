package feign.codec;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.Arrays;
import java.util.Date;
import feign.RequestTemplate;
import static feign.Util.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultEncoderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private final Encoder encoder = new Encoder.Default();

  @Test
  public void testEncodesStrings() throws Exception {
    String content = "This is my content";
    RequestTemplate template = new RequestTemplate();
    encoder.encode(content, String.class, template);
    assertEquals(content, new String(template.body(), UTF_8));
  }

  @Test
  public void testEncodesByteArray() throws Exception {
    byte[] content = {12, 34, 56};
    RequestTemplate template = new RequestTemplate();
    encoder.encode(content, byte[].class, template);
    assertTrue(Arrays.equals(content, template.body()));
  }

  @Test
  public void testRefusesToEncodeOtherTypes() throws Exception {
    thrown.expect(EncodeException.class);
    thrown.expectMessage("is not a type supported by this encoder.");

    encoder.encode(new Date(), Date.class, new RequestTemplate());
  }
}
