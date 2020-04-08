package feign.reactive;

import feign.Contract;
import feign.Param;
import feign.RequestLine;
import feign.reactive.ReactiveDelegatingContract;
import io.reactivex.Flowable;
import java.util.stream.Stream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactiveDelegatingContractTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void onlyReactiveReturnTypesSupported() {
    this.thrown.expect(IllegalArgumentException.class);
    Contract contract = new ReactiveDelegatingContract(new Contract.Default());
    contract.parseAndValidatateMetadata(TestSynchronousService.class);
  }

  @Test
  public void reactorTypes() {
    Contract contract = new ReactiveDelegatingContract(new Contract.Default());
    contract.parseAndValidatateMetadata(TestReactorService.class);
  }

  @Test
  public void reactivexTypes() {
    Contract contract = new ReactiveDelegatingContract(new Contract.Default());
    contract.parseAndValidatateMetadata(TestReactiveXService.class);
  }

  @Test
  public void streamsAreNotSupported() {
    this.thrown.expect(IllegalArgumentException.class);
    Contract contract = new ReactiveDelegatingContract(new Contract.Default());
    contract.parseAndValidatateMetadata(StreamsService.class);
  }

  public interface TestSynchronousService {
    @RequestLine("GET /version")
    String version();
  }

  public interface TestReactiveXService {
    @RequestLine("GET /version")
    Flowable<String> version();
  }


  public interface TestReactorService {
    @RequestLine("GET /version")
    Mono<String> version();

    @RequestLine("GET /users/{username}")
    Flux<String> user(@Param("username") String username);
  }

  public interface StreamsService {

    @RequestLine("GET /version")
    Mono<Stream<String>> version();
  }

}
