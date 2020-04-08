package feign.reactive;

import feign.InvocationHandlerFactory.MethodHandler;
import feign.Target;
import java.lang.reflect.Method;
import java.util.Map;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

public class ReactorInvocationHandler extends ReactiveInvocationHandler {
  private final Scheduler scheduler;

  ReactorInvocationHandler(Target<?> target,
      Map<Method, MethodHandler> dispatch,
      Scheduler scheduler) {
    super(target, dispatch);
    this.scheduler = scheduler;
  }

  @Override
  protected Publisher invoke(Method method, MethodHandler methodHandler, Object[] arguments) {
    Publisher<?> invocation = this.invokeMethod(methodHandler, arguments);
    if (Flux.class.isAssignableFrom(method.getReturnType())) {
      return Flux.from(invocation).subscribeOn(scheduler);
    } else if (Mono.class.isAssignableFrom(method.getReturnType())) {
      return Mono.from(invocation).subscribeOn(scheduler);
    }
    throw new IllegalArgumentException(
        "Return type " + method.getReturnType().getName() + " is not supported");
  }
}
