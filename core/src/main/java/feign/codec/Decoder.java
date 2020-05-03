package feign.codec;

import java.io.IOException;
import java.lang.reflect.Type;
import feign.Feign;
import feign.FeignException;
import feign.Response;
import feign.Util;

/**
 * 将HTTP响应解码为给定的{@code type}的单个对象.
 * 当{@link Response#status()}在2xx范围内且返回类型既不是{@code void}也不是{@code Response}时调用.
 * <p/>
 * 示例实现:<br>
 * <p/>
 *
 * <pre>
 * public class GsonDecoder implements Decoder {
 *   private final Gson gson = new Gson();
 *
 *   &#064;Override
 *   public Object decode(Response response, Type type) throws IOException {
 *     try {
 *       return gson.fromJson(response.body().asReader(), type);
 *     } catch (JsonIOException e) {
 *       if (e.getCause() != null &amp;&amp;
 *           e.getCause() instanceof IOException) {
 *         throw IOException.class.cast(e.getCause());
 *       }
 *       throw e;
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * <br/>
 * <h3>实现注意</h3> The {@code type} parameter will correspond to the
 * {@link java.lang.reflect.Method#getGenericReturnType() generic return type} of an
 * {@link feign.Target#type() interface} processed by {@link feign.Feign#newInstance(feign.Target)}.
 * When writing your implementation of Decoder, ensure you also test parameterized types such as
 * {@code
 * List<Foo>}. <br/>
 * <h3>关于异常抛出的注意事项</h3> Exceptions thrown by {@link Decoder}s get wrapped in a
 * {@link DecodeException} unless they are a subclass of {@link FeignException} already, and unless
 * the client was configured with {@link Feign.Builder#decode404()}.
 */
public interface Decoder {

  /**
   * Decodes an http response into an object corresponding to its
   * {@link java.lang.reflect.Method#getGenericReturnType() generic return type}. If you need to
   * wrap exceptions, please do so via {@link DecodeException}.
   *
   * @param response the response to decode
   * @param type {@link java.lang.reflect.Method#getGenericReturnType() generic return type} of the
   *        method corresponding to this {@code response}.
   * @return instance of {@code type}
   * @throws IOException will be propagated safely to the caller.
   * @throws DecodeException when decoding failed due to a checked exception besides IOException.
   * @throws FeignException when decoding succeeds, but conveys the operation failed.
   */
  Object decode(Response response, Type type) throws IOException, DecodeException, FeignException;

  /**
   * {@code Decoder}的默认实现.
   */
  public class Default extends StringDecoder {

    @Override
    public Object decode(Response response, Type type) throws IOException {
      if (response.status() == 404 || response.status() == 204) {
        return Util.emptyValueOf(type);
      }
      if (response.body() == null) {
        return null;
      }
      if (byte[].class.equals(type)) {
        return Util.toByteArray(response.body().asInputStream());
      }
      return super.decode(response, type);
    }
  }
}
