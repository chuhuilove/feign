package feign.benchmark;



import feign.Feign;
import feign.Logger;
import feign.Logger.Level;
import feign.Response;
import feign.Retryer;
import io.reactivex.netty.protocol.http.HttpHandlerNames;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import io.netty.buffer.ByteBuf;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.internal.http.HttpHeaders;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import rx.Observable;

@Measurement(iterations = 5, time = 1)
@Warmup(iterations = 10, time = 1)
@Fork(3)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class RealRequestBenchmarks {

  private static final int SERVER_PORT = 8765;
  private HttpServer<ByteBuf, ByteBuf> server;
  private OkHttpClient client;
  private FeignTestInterface okFeign;
  private Request queryRequest;

  @Setup
  public void setup() {

    server = HttpServer.newServer(SERVER_PORT)
        .start((request, response) -> null);
    client = new OkHttpClient();
    client.retryOnConnectionFailure();
    okFeign = Feign.builder()
        .client(new feign.okhttp.OkHttpClient(client))
        .logLevel(Level.NONE)
        .logger(new Logger.ErrorLogger())
        .retryer(new Retryer.Default())
        .target(FeignTestInterface.class, "http://localhost:" + SERVER_PORT);
    queryRequest = new Request.Builder()
        .url("http://localhost:" + SERVER_PORT + "/?Action=GetUser&Version=2010-05-08&limit=1")
        .build();
  }

  @TearDown
  public void tearDown() throws InterruptedException {
    server.shutdown();
  }

  /**
   * How fast can we execute get commands synchronously?
   */
  @Benchmark
  public okhttp3.Response query_baseCaseUsingOkHttp() throws IOException {
    okhttp3.Response result = client.newCall(queryRequest).execute();
    result.body().close();
    return result;
  }

  /**
   * How fast can we execute get commands synchronously using Feign?
   */
  @Benchmark
  public boolean query_feignUsingOkHttp() {
    /* auto close the response */
    try (Response ignored = okFeign.query()) {
      return true;
    }
  }
}
