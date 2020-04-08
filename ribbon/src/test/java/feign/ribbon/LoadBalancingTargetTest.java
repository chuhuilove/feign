package feign.ribbon;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Rule;
import org.junit.Test;
import java.io.IOException;
import java.net.URL;
import feign.Feign;
import feign.RequestLine;
import static com.netflix.config.ConfigurationManager.getConfigInstance;
import static org.junit.Assert.assertEquals;

public class LoadBalancingTargetTest {

  @Rule
  public final MockWebServer server1 = new MockWebServer();
  @Rule
  public final MockWebServer server2 = new MockWebServer();

  static String hostAndPort(URL url) {
    // our build slaves have underscores in their hostnames which aren't permitted by ribbon
    return "localhost:" + url.getPort();
  }

  @Test
  public void loadBalancingDefaultPolicyRoundRobin() throws IOException, InterruptedException {
    String name = "LoadBalancingTargetTest-loadBalancingDefaultPolicyRoundRobin";
    String serverListKey = name + ".ribbon.listOfServers";

    server1.enqueue(new MockResponse().setBody("success!"));
    server2.enqueue(new MockResponse().setBody("success!"));

    getConfigInstance().setProperty(serverListKey,
        hostAndPort(server1.url("").url()) + "," + hostAndPort(
            server2.url("").url()));

    try {
      LoadBalancingTarget<TestInterface> target =
          LoadBalancingTarget.create(TestInterface.class, "http://" + name);
      TestInterface api = Feign.builder().target(target);

      api.post();
      api.post();

      assertEquals(1, server1.getRequestCount());
      assertEquals(1, server2.getRequestCount());
      // TODO: verify ribbon stats match
      // assertEquals(target.lb().getLoadBalancerStats().getSingleServerStat())
    } finally {
      getConfigInstance().clearProperty(serverListKey);
    }
  }

  @Test
  public void loadBalancingTargetPath() throws InterruptedException {
    String name = "LoadBalancingTargetTest-loadBalancingDefaultPolicyRoundRobin";
    String serverListKey = name + ".ribbon.listOfServers";

    server1.enqueue(new MockResponse().setBody("success!"));

    getConfigInstance().setProperty(serverListKey,
        hostAndPort(server1.url("").url()));

    try {
      LoadBalancingTarget<TestInterface> target =
          LoadBalancingTarget.create(TestInterface.class, "http://" + name + "/context-path");
      TestInterface api = Feign.builder().target(target);

      api.get();

      assertEquals("http:///context-path", target.url());
      assertEquals("/context-path/servers", server1.takeRequest().getPath());
    } finally {
      getConfigInstance().clearProperty(serverListKey);
    }
  }

  interface TestInterface {

    @RequestLine("POST /")
    void post();

    @RequestLine("GET /servers")
    void get();
  }
}
