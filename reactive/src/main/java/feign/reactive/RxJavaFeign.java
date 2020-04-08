package feign.reactive;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import feign.Feign;
import feign.InvocationHandlerFactory;
import feign.Target;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class RxJavaFeign extends ReactiveFeign {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends ReactiveFeign.Builder {

    private Scheduler scheduler = Schedulers.trampoline();

    @Override
    public Feign build() {
      super.invocationHandlerFactory(new RxJavaInvocationHandlerFactory(scheduler));
      return super.build();
    }

    @Override
    public Builder invocationHandlerFactory(InvocationHandlerFactory invocationHandlerFactory) {
      throw new UnsupportedOperationException(
          "Invocation Handler Factory overrides are not supported.");
    }

    public Builder scheduleOn(Scheduler scheduler) {
      this.scheduler = scheduler;
      return this;
    }
  }

  private static class RxJavaInvocationHandlerFactory implements InvocationHandlerFactory {
    private final Scheduler scheduler;

    private RxJavaInvocationHandlerFactory(Scheduler scheduler) {
      this.scheduler = scheduler;
    }

    @Override
    public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
      return new RxJavaInvocationHandler(target, dispatch, scheduler);
    }
  }

}
