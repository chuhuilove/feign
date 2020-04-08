package feign.querymap;

import feign.QueryMapEncoder;
import feign.codec.EncodeException;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * the query map will be generated using java beans accessible getter property as query parameter
 * names.
 *
 * eg: "/uri?name={name}&number={number}"
 *
 * order of included query parameters not guaranteed, and as usual, if any value is null, it will be
 * left out
 */
public class BeanQueryMapEncoder implements QueryMapEncoder {
  private final Map<Class<?>, ObjectParamMetadata> classToMetadata =
      new HashMap<Class<?>, ObjectParamMetadata>();

  @Override
  public Map<String, Object> encode(Object object) throws EncodeException {
    try {
      ObjectParamMetadata metadata = getMetadata(object.getClass());
      Map<String, Object> propertyNameToValue = new HashMap<String, Object>();
      for (PropertyDescriptor pd : metadata.objectProperties) {
        Object value = pd.getReadMethod().invoke(object);
        if (value != null && value != object) {
          propertyNameToValue.put(pd.getName(), value);
        }
      }
      return propertyNameToValue;
    } catch (IllegalAccessException | IntrospectionException | InvocationTargetException e) {
      throw new EncodeException("Failure encoding object into query map", e);
    }
  }

  private ObjectParamMetadata getMetadata(Class<?> objectType) throws IntrospectionException {
    ObjectParamMetadata metadata = classToMetadata.get(objectType);
    if (metadata == null) {
      metadata = ObjectParamMetadata.parseObjectType(objectType);
      classToMetadata.put(objectType, metadata);
    }
    return metadata;
  }

  private static class ObjectParamMetadata {

    private final List<PropertyDescriptor> objectProperties;

    private ObjectParamMetadata(List<PropertyDescriptor> objectProperties) {
      this.objectProperties = Collections.unmodifiableList(objectProperties);
    }

    private static ObjectParamMetadata parseObjectType(Class<?> type)
        throws IntrospectionException {
      List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();

      for (PropertyDescriptor pd : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
        boolean isGetterMethod = pd.getReadMethod() != null && !"class".equals(pd.getName());
        if (isGetterMethod) {
          properties.add(pd);
        }
      }

      return new ObjectParamMetadata(properties);
    }
  }
}
