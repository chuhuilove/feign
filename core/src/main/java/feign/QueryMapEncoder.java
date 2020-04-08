package feign;

import feign.querymap.FieldQueryMapEncoder;
import feign.querymap.BeanQueryMapEncoder;
import java.util.Map;

/**
 * A QueryMapEncoder encodes Objects into maps of query parameter names to values.
 *
 * @see FieldQueryMapEncoder
 * @see BeanQueryMapEncoder
 *
 */
public interface QueryMapEncoder {

  /**
   * Encodes the given object into a query map.
   *
   * @param object the object to encode
   * @return the map represented by the object
   */
  Map<String, Object> encode(Object object);

  /**
   * @deprecated use {@link BeanQueryMapEncoder} instead. default encoder uses reflection to inspect
   *             provided objects Fields to expand the objects values into a query string. If you
   *             prefer that the query string be built using getter and setter methods, as defined
   *             in the Java Beans API, please use the {@link BeanQueryMapEncoder}
   */
  class Default extends FieldQueryMapEncoder {
  }
}
