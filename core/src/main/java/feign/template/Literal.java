package feign.template;

/**
 * URI Template Literal.
 */
class Literal implements TemplateChunk {

  private final String value;

  /**
   * Create a new Literal.
   *
   * @param value of the literal.
   * @return the new Literal.
   */
  public static Literal create(String value) {
    return new Literal(value);
  }

  /**
   * Create a new Literal.
   *
   * @param value of the literal.
   */
  Literal(String value) {
    if (value == null || value.isEmpty()) {
      throw new IllegalArgumentException("a value is required.");
    }
    this.value = value;
  }

  @Override
  public String getValue() {
    return this.value;
  }
}
