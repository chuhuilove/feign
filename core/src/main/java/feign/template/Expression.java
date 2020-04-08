package feign.template;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URI Template Expression.
 */
abstract class Expression implements TemplateChunk {

  private String name;
  private Pattern pattern;

  /**
   * Create a new Expression.
   *
   * @param name of the variable
   * @param pattern the resolved variable must adhere to, optional.
   */
  Expression(String name, String pattern) {
    this.name = name;
    Optional.ofNullable(pattern).ifPresent(s -> this.pattern = Pattern.compile(s));
  }

  abstract String expand(Object variable, boolean encode);

  public String getName() {
    return this.name;
  }

  Pattern getPattern() {
    return pattern;
  }

  /**
   * Checks if the provided value matches the variable pattern, if one is defined. Always true if no
   * pattern is defined.
   *
   * @param value to check.
   * @return true if it matches.
   */
  boolean matches(String value) {
    if (pattern == null) {
      return true;
    }
    return pattern.matcher(value).matches();
  }

  @Override
  public String getValue() {
    if (this.pattern != null) {
      return "{" + this.name + ":" + this.pattern + "}";
    }
    return "{" + this.name + "}";
  }

  @Override
  public String toString() {
    return this.getValue();
  }
}
