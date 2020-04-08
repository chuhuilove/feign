package feign.hystrix;

import com.netflix.hystrix.HystrixCommand;
import rx.Subscription;
import java.util.concurrent.CompletableFuture;

final class ObservableCompletableFuture<T> extends CompletableFuture<T> {

  private final Subscription sub;

  ObservableCompletableFuture(final HystrixCommand<T> command) {
    this.sub = command.toObservable().single().subscribe(ObservableCompletableFuture.this::complete,
        ObservableCompletableFuture.this::completeExceptionally);
  }


  @Override
  public boolean cancel(final boolean b) {
    sub.unsubscribe();
    return super.cancel(b);
  }
}
