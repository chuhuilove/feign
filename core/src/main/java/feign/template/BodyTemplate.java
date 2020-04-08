package feign.template;

import feign.Util;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Template for @{@link feign.Body} annotated Templates. Unresolved expressions are preserved as
 * literals and literals are not URI encoded.
 */
public final class BodyTemplate extends Template {

  private static final String JSON_TOKEN_START = "{";
  private static final String JSON_TOKEN_END = "}";
  private static final String JSON_TOKEN_START_ENCODED = "%7B";
  private static final String JSON_TOKEN_END_ENCODED = "%7D";
  private boolean json = false;

  /**
   * Create a new Body Template.
   *
   * @param template to parse.
   * @return a Body Template instance.
   */
  public static BodyTemplate create(String template) {
    return new BodyTemplate(template, Util.UTF_8);
  }

  private BodyTemplate(String value, Charset charset) {
    super(value, ExpansionOptions.ALLOW_UNRESOLVED, EncodingOptions.NOT_REQUIRED, false, charset);
    if (value.startsWith(JSON_TOKEN_START_ENCODED) && value.endsWith(JSON_TOKEN_END_ENCODED)) {
      this.json = true;
    }
  }

  @Override
  public String expand(Map<String, ?> variables) {
    String expanded = super.expand(variables);
    if (this.json) {
      /* decode only the first and last character */
      StringBuilder sb = new StringBuilder();
      sb.append(JSON_TOKEN_START);
      sb.append(expanded,
          expanded.indexOf(JSON_TOKEN_START_ENCODED) + JSON_TOKEN_START_ENCODED.length(),
          expanded.lastIndexOf(JSON_TOKEN_END_ENCODED));
      sb.append(JSON_TOKEN_END);
      return sb.toString();
    }
    return expanded;
  }


}
