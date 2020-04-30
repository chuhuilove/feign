package feign;

/**
 * Zero or more {@code RequestInterceptors} may be configured for purposes such as adding headers to
 * all requests.
 * 可以配置零个或多个{@code RequestInterceptors},用于向所有请求添加请求头等信息.
 * 对于拦截器的应用顺序,不做任何保证.
 * 一旦拦截器被应用,就会调用{@link Target#apply(RequestTemplate)}来创建通过
 * {@link Client#execute(Request, feign.Request.Options)}发送的不变的HTTP请求.<br>
 * <br>
 * For example: <br>
 * 
 * <pre>
 * public void apply(RequestTemplate input) {
 *   input.header(&quot;X-Auth&quot;, currentToken);
 * }
 * </pre>
 *
 * <br>
 * <br>
 * <b>Configuration</b><br>
 * <br>
 * {@code RequestInterceptors} are configured via {@link Feign.Builder#requestInterceptors}. <br>
 * <br>
 * <b>Implementation notes</b><br>
 * <br>
 * Do not add parameters, such as {@code /path/{foo}/bar } in your implementation of
 * {@link #apply(RequestTemplate)}. <br>
 * Interceptors are applied after the template's parameters are
 * {@link RequestTemplate#resolve(java.util.Map) resolved}. This is to ensure that you can implement
 * signatures are interceptors. <br>
 * <br>
 * <br>
 * <b>Relationship to Retrofit 1.x</b><br>
 * <br>
 * This class is similar to {@code RequestInterceptor.intercept()}, except that the implementation
 * can read, remove, or otherwise mutate any part of the request template.
 */
public interface RequestInterceptor {

  /**
   * Called for every request. Add data using methods on the supplied {@link RequestTemplate}.
   */
  void apply(RequestTemplate template);
}
