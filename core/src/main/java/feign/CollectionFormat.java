package feign;

import feign.template.UriUtils;
import java.nio.charset.Charset;
import java.util.Collection;

/**
 * Various ways to encode collections in URL parameters.
 *
 * <p>
 * These specific cases are inspired by the <a href="http://swagger.io/specification/">OpenAPI
 * specification</a>.
 * </p>
 */
public enum CollectionFormat {
  /** Comma separated values, eg foo=bar,baz */
  CSV(","),
  /** Space separated values, eg foo=bar baz */
  SSV(" "),
  /** Tab separated values, eg foo=bar[tab]baz */
  TSV("\t"),
  /** Values separated with the pipe (|) character, eg foo=bar|baz */
  PIPES("|"),
  /** Parameter name repeated for each value, eg foo=bar&foo=baz */
  // Using null as a special case since there is no single separator character
  EXPLODED(null);

  private final String separator;

  CollectionFormat(String separator) {
    this.separator = separator;
  }

  /**
   * Joins the field and possibly multiple values with the given separator.
   *
   * <p>
   * Calling EXPLODED.join("foo", ["bar"]) will return "foo=bar".
   * </p>
   *
   * <p>
   * Calling CSV.join("foo", ["bar", "baz"]) will return "foo=bar,baz".
   * </p>
   *
   * <p>
   * Null values are treated somewhat specially. With EXPLODED, the field is repeated without any
   * "=" for backwards compatibility. With all other formats, null values are not included in the
   * joined value list.
   * </p>
   *
   * @param field The field name corresponding to these values.
   * @param values A collection of value strings for the given field.
   * @param charset to encode the sequence
   * @return The formatted char sequence of the field and joined values. If the value collection is
   *         empty, an empty char sequence will be returned.
   */
  public CharSequence join(String field, Collection<String> values, Charset charset) {
    StringBuilder builder = new StringBuilder();
    int valueCount = 0;
    for (String value : values) {
      if (separator == null) {
        // exploded
        builder.append(valueCount++ == 0 ? "" : "&");
        builder.append(UriUtils.queryEncode(field, charset));
        if (value != null) {
          builder.append('=');
          builder.append(UriUtils.queryEncode(value, charset));
        }
      } else {
        // delimited with a separator character
        if (builder.length() == 0) {
          builder.append(UriUtils.queryEncode(field, charset));
        }
        if (value == null) {
          continue;
        }
        builder.append(valueCount++ == 0 ? "=" : separator);
        builder.append(UriUtils.queryEncode(value, charset));
      }
    }
    return builder;
  }
}
