package feign;

import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Expands the uri template supplied in the {@code value}, permitting path and query variables, or
 * just the http method. Templates should conform to
 * <a href="https://tools.ietf.org/html/rfc6570">RFC 6570</a>. Support is limited to Simple String
 * expansion and Reserved Expansion (Level 1 and Level 2) expressions.
 */
@java.lang.annotation.Target(METHOD)
@Retention(RUNTIME)
public @interface RequestLine {

  String value();

  boolean decodeSlash() default true;

  CollectionFormat collectionFormat() default CollectionFormat.EXPLODED;
}
