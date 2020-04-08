package feign.template;

/**
 * Represents the parts of a URI Template.
 */
@FunctionalInterface
interface TemplateChunk {

  String getValue();

}
