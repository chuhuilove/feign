package feign.reactive;

import feign.InvocationHandlerFactory.MethodHandler;
import feign.Target;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import java.lang.reflect.Method;
import java.util.Map;
import org.reactivestreams.Publisher;

public class RxJavaInvocationHandler extends ReactiveInvocationHandler {
  private final Scheduler scheduler;

  RxJavaInvocationHandler(Target<?> target,
      Map<Method, MethodHandler> dispatch,
      Scheduler scheduler) {
    super(target, dispatch);
    this.scheduler = scheduler;
  }

  @Override
  protected Publisher invoke(Method method, MethodHandler methodHandler, Object[] arguments) {
    return Flowable.fromPublisher(this.invokeMethod(methodHandler, arguments))
        .observeOn(scheduler);
  }
}
