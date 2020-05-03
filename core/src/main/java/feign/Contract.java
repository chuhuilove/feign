package feign;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import feign.Request.HttpMethod;
import static feign.Util.checkState;
import static feign.Util.emptyToNull;

/**
 * Defines what annotations and values are valid on interfaces. 定义接口上有效的注解和值.
 */
public interface Contract {

  /**
   * Called to parse the methods in the class that are linked to HTTP requests.
   *
   * @param targetType {@link feign.Target#type() type} of the Feign interface.
   */
  // TODO: break this and correct spelling at some point
  List<MethodMetadata> parseAndValidatateMetadata(Class<?> targetType);

  abstract class BaseContract implements Contract {

    /**
     * 传递进来一个接口类型
     * 
     * @param targetType {@link feign.Target#type() type} of the Feign interface.
     * @return
     */
    @Override
    public List<MethodMetadata> parseAndValidatateMetadata(Class<?> targetType) {
      // targetType.getTypeParameters()不存在

      checkState(targetType.getTypeParameters().length == 0, "Parameterized types unsupported: %s",
          targetType.getSimpleName());

      // 这个类只能继承一个接口
      checkState(targetType.getInterfaces().length <= 1, "Only single inheritance supported: %s",
          targetType.getSimpleName());

      // 如果已经继承了一个接口....
      if (targetType.getInterfaces().length == 1) {
        // 校验那个接口是不是只继承了一个接口....
        checkState(targetType.getInterfaces()[0].getInterfaces().length == 0,
            "Only single-level inheritance supported: %s",
            targetType.getSimpleName());
      }

      Map<String, MethodMetadata> result = new LinkedHashMap<String, MethodMetadata>();

      // 获取接口中定义的所有方法.
      for (Method method : targetType.getMethods()) {

        // 判断方法的类型,以确定是否应该解析
        // 如果方法是继承自Object的,或者是静态的,或者是默认的,则不进行解析
        if (method.getDeclaringClass() == Object.class ||
            (method.getModifiers() & Modifier.STATIC) != 0 ||
            Util.isDefault(method)) {
          continue;
        }

        //
        //
        /**
         * 将方法所在的类,方法本身,以及方法的参数,进行解析出来 解析的结果封装成{@link MethodMetadata}.
         * 如果本类是{@code SpringMvcContract},
         * 由于{@code SpringMvcContract}重写了{@link #parseAndValidateMetadata(Class, Method)}
         * 所以会在实际运行的过程中,从{@code SpringMvcContract}调到本类实现的
         * {@link #parseAndValidateMetadata(Class, Method)}中.
         *
         */
        MethodMetadata metadata = parseAndValidateMetadata(targetType, method);
        // 校验一下,运行方法重载,但是不允许方法签名重载
        checkState(!result.containsKey(metadata.configKey()), "Overrides unsupported: %s",
            metadata.configKey());
        result.put(metadata.configKey(), metadata);
      }
      return new ArrayList<>(result.values());
    }

    /**
     * @deprecated use {@link #parseAndValidateMetadata(Class, Method)} instead.
     */
    @Deprecated
    public MethodMetadata parseAndValidatateMetadata(Method method) {
      return parseAndValidateMetadata(method.getDeclaringClass(), method);
    }

    /**
     * Called indirectly by {@link #parseAndValidatateMetadata(Class)}.
     */
    protected MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {

      MethodMetadata data = new MethodMetadata();
      // 解析方法的返回类型
      data.returnType(Types.resolve(targetType, targetType, method.getGenericReturnType()));
      // 设置方法的configKey
      data.configKey(Feign.configKey(targetType, method));

      if (targetType.getInterfaces().length == 1) {
        processAnnotationOnClass(data, targetType.getInterfaces()[0]);
      }
      /**
       * 在{@link Default#processAnnotationOnClass(MethodMetadata, Class)} 中,解析主接口上的@Headers注解
       * 在{@link SpringMvcContract#processAnnotationOnClass(MethodMetadata, Class)}
       * 中,解析主接口上的@RequestMapping注解
       *
       */
      processAnnotationOnClass(data, targetType);


      for (Annotation methodAnnotation : method.getAnnotations()) {
        /**
         * 获取方法上的所有注解,开始进行解析,解析结果存储到{@link MethodMetadata}中.
         *
         * 在{@link Default#processAnnotationOnClass(MethodMetadata, Class)}中,
         * 解析接口方法上的{@code @Headers},{@code @ReadLine}和{@code @Body}注解
         * 在{@link SpringMvcContract#processAnnotationOnClass(MethodMetadata, Class)}中,
         * 解析接口方法上的{@code @RequestMapping}注解
         */
        processAnnotationOnMethod(data, methodAnnotation, method);
      }
      // 校验方法上的请求方式
      checkState(data.template().method() != null,
          "Method %s not annotated with HTTP method type (ex. GET, POST)",
          method.getName());
      // 获取方法上的参数类型
      Class<?>[] parameterTypes = method.getParameterTypes();
      Type[] genericParameterTypes = method.getGenericParameterTypes();
      // 获取方法上参数类型上的注解
      Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      int count = parameterAnnotations.length;
      for (int i = 0; i < count; i++) {
        boolean isHttpAnnotation = false;
        if (parameterAnnotations[i] != null) {
          isHttpAnnotation = processAnnotationsOnParameter(data, parameterAnnotations[i], i);
        }
        if (parameterTypes[i] == URI.class) {
          data.urlIndex(i);
        } else if (!isHttpAnnotation && parameterTypes[i] != Request.Options.class) {
          checkState(data.formParams().isEmpty(),
              "Body parameters cannot be used with form parameters.");
          checkState(data.bodyIndex() == null, "Method has too many Body parameters: %s", method);
          data.bodyIndex(i);
          data.bodyType(Types.resolve(targetType, targetType, genericParameterTypes[i]));
        }
      }

      if (data.headerMapIndex() != null) {
        checkMapString("HeaderMap", parameterTypes[data.headerMapIndex()],
            genericParameterTypes[data.headerMapIndex()]);
      }

      if (data.queryMapIndex() != null) {
        if (Map.class.isAssignableFrom(parameterTypes[data.queryMapIndex()])) {
          checkMapKeys("QueryMap", genericParameterTypes[data.queryMapIndex()]);
        }
      }

      return data;
    }

    private static void checkMapString(String name, Class<?> type, Type genericType) {
      checkState(Map.class.isAssignableFrom(type),
          "%s parameter must be a Map: %s", name, type);
      checkMapKeys(name, genericType);
    }

    private static void checkMapKeys(String name, Type genericType) {
      Class<?> keyClass = null;

      // assume our type parameterized
      if (ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
        Type[] parameterTypes = ((ParameterizedType) genericType).getActualTypeArguments();
        keyClass = (Class<?>) parameterTypes[0];
      } else if (genericType instanceof Class<?>) {
        // raw class, type parameters cannot be inferred directly, but we can scan any extended
        // interfaces looking for any explict types
        Type[] interfaces = ((Class) genericType).getGenericInterfaces();
        if (interfaces != null) {
          for (Type extended : interfaces) {
            if (ParameterizedType.class.isAssignableFrom(extended.getClass())) {
              // use the first extended interface we find.
              Type[] parameterTypes = ((ParameterizedType) extended).getActualTypeArguments();
              keyClass = (Class<?>) parameterTypes[0];
              break;
            }
          }
        }
      }

      if (keyClass != null) {
        checkState(String.class.equals(keyClass),
            "%s key must be a String: %s", name, keyClass.getSimpleName());
      }
    }


    /**
     * Called by parseAndValidateMetadata twice, first on the declaring class, then on the target
     * type (unless they are the same).
     *
     * 在{@link Default#processAnnotationOnClass(MethodMetadata, Class)}中, 解析主接口上的{@code @Headers}注解
     * 在{@link SpringMvcContract#processAnnotationOnClass(MethodMetadata, Class)}中,
     * 解析主接口上的{@code @RequestMapping}注解
     *
     * @param data metadata collected so far relating to the current java method.
     * @param clz the class to process
     */
    protected abstract void processAnnotationOnClass(MethodMetadata data, Class<?> clz);

    /**
     * 在{@link Default#processAnnotationOnClass(MethodMetadata, Class)}中,
     * 解析接口方法上的{@link Headers},{@link RequestLine}和{@link Body}注解
     * 在{@link SpringMvcContract#processAnnotationOnClass(MethodMetadata, Class)}中,
     * 解析接口方法上的{@code @RequestMapping}注解
     *
     * @param data metadata collected so far relating to the current java method.
     * @param annotation annotations present on the current method annotation.
     * @param method method currently being processed.
     */
    protected abstract void processAnnotationOnMethod(MethodMetadata data,
                                                      Annotation annotation,
                                                      Method method);

    /**
     * 在{@link Default#processAnnotationOnClass(MethodMetadata, Class)}中,
     * 解析接口方法上的{@link Param},{@link QueryMap}和{@link HeaderMap}注解
     * 在{@link SpringMvcContract#processAnnotationOnClass(MethodMetadata, Class)}中,
     * 解析接口方法上的{@code @RequestMapping}注解
     *
     * @param data metadata collected so far relating to the current java method.
     * @param annotations annotations present on the current parameter annotation.
     * @param paramIndex if you find a name in {@code annotations}, call
     *        {@link #nameParam(MethodMetadata, String, int)} with this as the last parameter.
     * @return true if you called {@link #nameParam(MethodMetadata, String, int)} after finding an
     *         http-relevant annotation.
     */
    protected abstract boolean processAnnotationsOnParameter(MethodMetadata data,
                                                             Annotation[] annotations,
                                                             int paramIndex);

    /**
     * links a parameter name to its index in the method signature.
     */
    protected void nameParam(MethodMetadata data, String name, int i) {
      Collection<String> names =
          data.indexToName().containsKey(i) ? data.indexToName().get(i) : new ArrayList<String>();
      names.add(name);
      data.indexToName().put(i, names);
    }
  }

  class Default extends BaseContract {

    static final Pattern REQUEST_LINE_PATTERN = Pattern.compile("^([A-Z]+)[ ]*(.*)$");

    @Override
    protected void processAnnotationOnClass(MethodMetadata data, Class<?> targetType) {
      // 默认的配置中,判断主接口上是否有{@code Headers}注解,这个注解
      if (targetType.isAnnotationPresent(Headers.class)) {

        // 从Header中获取设置的请求头
        String[] headersOnType = targetType.getAnnotation(Headers.class).value();
        checkState(headersOnType.length > 0, "Headers annotation was empty on type %s.",
            targetType.getName());
        // 将得到的请求头,转换成map
        Map<String, Collection<String>> headers = toMap(headersOnType);
        headers.putAll(data.template().headers());
        // 清除掉之前原有的请求头....
        data.template().headers(null); // to clear
        data.template().headers(headers);
      }
    }

    @Override
    protected void processAnnotationOnMethod(MethodMetadata data,
                                             Annotation methodAnnotation,
                                             Method method) {
      Class<? extends Annotation> annotationType = methodAnnotation.annotationType();

      if (annotationType == RequestLine.class) {
        // 如果注解类型是{@code RequestLine}
        /**
         * 注解是{@link RequestLine}, 获取到{@link RequestLine#value()}.
         */
        String requestLine = RequestLine.class.cast(methodAnnotation).value();
        checkState(emptyToNull(requestLine) != null,
            "RequestLine annotation was empty on method %s.", method.getName());

        Matcher requestLineMatcher = REQUEST_LINE_PATTERN.matcher(requestLine);
        if (!requestLineMatcher.find()) {
          throw new IllegalStateException(String.format(
              "RequestLine annotation didn't start with an HTTP verb on method %s",
              method.getName()));
        } else {
          // 从value中获取到请求方法
          data.template().method(HttpMethod.valueOf(requestLineMatcher.group(1)));
          // 从value中获取到请求url
          data.template().uri(requestLineMatcher.group(2));
        }

        data.template().decodeSlash(RequestLine.class.cast(methodAnnotation).decodeSlash());
        data.template()
            .collectionFormat(RequestLine.class.cast(methodAnnotation).collectionFormat());

      } else if (annotationType == Body.class) {
        // 如果注解类型是{@code Body}
        // 获取到Body值
        String body = Body.class.cast(methodAnnotation).value();
        checkState(emptyToNull(body) != null, "Body annotation was empty on method %s.",
            method.getName());
        if (body.indexOf('{') == -1) {
          data.template().body(body);
        } else {
          data.template().bodyTemplate(body);
        }
      } else if (annotationType == Headers.class) {
        // 如果注解类型是{@code Headers}
        // 解析请求头
        String[] headersOnMethod = Headers.class.cast(methodAnnotation).value();
        checkState(headersOnMethod.length > 0, "Headers annotation was empty on method %s.",
            method.getName());
        data.template().headers(toMap(headersOnMethod));
      }
    }

    @Override
    protected boolean processAnnotationsOnParameter(MethodMetadata data,
                                                    Annotation[] annotations,
                                                    int paramIndex) {
      boolean isHttpAnnotation = false;
      for (Annotation annotation : annotations) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        if (annotationType == Param.class) {
          Param paramAnnotation = (Param) annotation;
          String name = paramAnnotation.value();
          checkState(emptyToNull(name) != null, "Param annotation was empty on param %s.",
              paramIndex);
          nameParam(data, name, paramIndex);
          Class<? extends Param.Expander> expander = paramAnnotation.expander();
          if (expander != Param.ToStringExpander.class) {
            data.indexToExpanderClass().put(paramIndex, expander);
          }
          data.indexToEncoded().put(paramIndex, paramAnnotation.encoded());
          isHttpAnnotation = true;
          if (!data.template().hasRequestVariable(name)) {
            data.formParams().add(name);
          }
        } else if (annotationType == QueryMap.class) {
          checkState(data.queryMapIndex() == null,
              "QueryMap annotation was present on multiple parameters.");
          data.queryMapIndex(paramIndex);
          data.queryMapEncoded(QueryMap.class.cast(annotation).encoded());
          isHttpAnnotation = true;
        } else if (annotationType == HeaderMap.class) {
          checkState(data.headerMapIndex() == null,
              "HeaderMap annotation was present on multiple parameters.");
          data.headerMapIndex(paramIndex);
          isHttpAnnotation = true;
        }
      }
      return isHttpAnnotation;
    }

    private static Map<String, Collection<String>> toMap(String[] input) {
      Map<String, Collection<String>> result =
          new LinkedHashMap<String, Collection<String>>(input.length);
      for (String header : input) {
        int colon = header.indexOf(':');
        String name = header.substring(0, colon);
        if (!result.containsKey(name)) {
          result.put(name, new ArrayList<String>(1));
        }
        result.get(name).add(header.substring(colon + 1).trim());
      }
      return result;
    }
  }
}
