package feign;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Expands headers supplied in the {@code value}. Variables to the the right of the colon are
 * expanded. <br>
 *
 * 为接口或者方法,设置请求头 这个注解可以加在接口类上,也可以添加在方法上, 如果加在接口上,则接口里面的方法,会全部拥有这个请求头,
 *
 * 
 * <pre>
 * &#64;Headers("Content-Type: application/xml")
 * interface SoapApi {
 * ...   
 * &#64;RequestLine("GET /")
 * &#64;Headers("Cache-Control: max-age=640000")
 * ...
 *
 * &#64;RequestLine("POST /")
 * &#64;Headers({
 *   "X-Foo: Bar",
 *   "X-Ping: {token}"
 * }) void post(&#64;Param("token") String token);
 * ...
 * </pre>
 * 
 * <br>
 * <strong>Notes:</strong>
 * <ul>
 * <li>如果你想在Header中使用花括号,请首先使用urlencode.</li>
 * <li>标头不会互相覆盖. 具有相同名称的所有标头将包含在请求中。</li>
 * </ul>
 * <br>
 * <b>Relationship to JAXRS</b><br>
 * <br>
 * The following two forms are identical. <br>
 * <br>
 * Feign:
 * 
 * <pre>
 * &#64;RequestLine("POST /")
 * &#64;Headers({
 *   "X-Ping: {token}"
 * }) void post(&#64;Named("token") String token);
 * ...
 * </pre>
 * 
 * <br>
 * JAX-RS:
 * 
 * <pre>
 * &#64;POST &#64;Path("/")
 * void post(&#64;HeaderParam("X-Ping") String token);
 * ...
 * </pre>
 */
@Target({METHOD, TYPE})
@Retention(RUNTIME)
public @interface Headers {

  String[] value();
}
