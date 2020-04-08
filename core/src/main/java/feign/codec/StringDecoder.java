package feign.codec;

import java.io.IOException;
import java.lang.reflect.Type;
import feign.Response;
import feign.Util;
import static java.lang.String.format;

public class StringDecoder implements Decoder {

  @Override
  public Object decode(Response response, Type type) throws IOException {
    Response.Body body = response.body();
    if (body == null) {
      return null;
    }
    if (String.class.equals(type)) {
      return Util.toString(body.asReader());
    }
    throw new DecodeException(response.status(),
        format("%s is not a type supported by this decoder.", type), response.request());
  }
}
