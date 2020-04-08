package feign.jaxrs2;

import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import feign.jaxrs.JAXRSContract;
import java.lang.annotation.Annotation;

/**
 * Please refer to the <a href="https://github.com/Netflix/feign/tree/master/feign-jaxrs2">Feign
 * JAX-RS 2 README</a>.
 */
public final class JAXRS2Contract extends JAXRSContract {
  @Override
  protected boolean isUnsupportedHttpParameterAnnotation(Annotation parameterAnnotation) {
    Class<? extends Annotation> annotationType = parameterAnnotation.annotationType();

    // masc20180327. parameter with unsupported jax-rs annotations should not be passed as body
    // params.
    // this will prevent interfaces from becoming unusable entirely due to single (unsupported)
    // endpoints.
    // https://github.com/OpenFeign/feign/issues/669
    return (annotationType == Suspended.class ||
        annotationType == Context.class);
  }
}
