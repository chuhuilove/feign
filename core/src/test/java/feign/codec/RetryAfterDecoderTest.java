package feign.codec;

import static feign.codec.ErrorDecoder.RetryAfterDecoder.RFC822_FORMAT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import feign.codec.ErrorDecoder.RetryAfterDecoder;
import java.text.ParseException;
import org.junit.Test;

public class RetryAfterDecoderTest {

  private RetryAfterDecoder decoder = new RetryAfterDecoder(RFC822_FORMAT) {
    protected long currentTimeMillis() {
      try {
        return RFC822_FORMAT.parse("Sat, 1 Jan 2000 00:00:00 GMT").getTime();
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }
    }
  };

  @Test
  public void malformDateFailsGracefully() {
    assertFalse(decoder.apply("Fri, 31 Dec 1999 23:59:59 ZBW") != null);
  }

  @Test
  public void rfc822Parses() throws ParseException {
    assertEquals(RFC822_FORMAT.parse("Fri, 31 Dec 1999 23:59:59 GMT"),
        decoder.apply("Fri, 31 Dec 1999 23:59:59 GMT"));
  }

  @Test
  public void relativeSecondsParses() throws ParseException {
    assertEquals(RFC822_FORMAT.parse("Sun, 2 Jan 2000 00:00:00 GMT"), decoder.apply("86400"));
  }

  @Test
  public void relativeSecondsParseDecimalIntegers() throws ParseException {
    assertEquals(RFC822_FORMAT.parse("Sun, 2 Jan 2000 00:00:00 GMT"), decoder.apply("86400.0"));
  }
}
