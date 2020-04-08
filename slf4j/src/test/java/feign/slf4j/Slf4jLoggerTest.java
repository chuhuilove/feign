package feign.slf4j;

import feign.Request.HttpMethod;
import feign.Util;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.Collections;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;

public class Slf4jLoggerTest {

  private static final String CONFIG_KEY = "someMethod()";
  private static final Request REQUEST =
      new RequestTemplate().method(HttpMethod.GET).target("http://api.example.com")
          .resolve(Collections.emptyMap()).request();
  private static final Response RESPONSE =
      Response.builder()
          .status(200)
          .reason("OK")
          .request(Request.create(HttpMethod.GET, "/api", Collections.emptyMap(), null, Util.UTF_8))
          .headers(Collections.<String, Collection<String>>emptyMap())
          .body(new byte[0])
          .build();
  @Rule
  public final RecordingSimpleLogger slf4j = new RecordingSimpleLogger();
  private Slf4jLogger logger;

  @Test
  public void useFeignLoggerByDefault() throws Exception {
    slf4j.logLevel("debug");
    slf4j.expectMessages(
        "DEBUG feign.Logger - [someMethod] This is my message" + System.lineSeparator());

    logger = new Slf4jLogger();
    logger.log(CONFIG_KEY, "This is my message");
  }

  @Test
  public void useLoggerByNameIfRequested() throws Exception {
    slf4j.logLevel("debug");
    slf4j.expectMessages(
        "DEBUG named.logger - [someMethod] This is my message" + System.lineSeparator());

    logger = new Slf4jLogger("named.logger");
    logger.log(CONFIG_KEY, "This is my message");
  }

  @Test
  public void useLoggerByClassIfRequested() throws Exception {
    slf4j.logLevel("debug");
    slf4j.expectMessages(
        "DEBUG feign.Feign - [someMethod] This is my message" + System.lineSeparator());

    logger = new Slf4jLogger(Feign.class);
    logger.log(CONFIG_KEY, "This is my message");
  }

  @Test
  public void useSpecifiedLoggerIfRequested() throws Exception {
    slf4j.logLevel("debug");
    slf4j.expectMessages(
        "DEBUG specified.logger - [someMethod] This is my message" + System.lineSeparator());

    logger = new Slf4jLogger(LoggerFactory.getLogger("specified.logger"));
    logger.log(CONFIG_KEY, "This is my message");
  }

  @Test
  public void logOnlyIfDebugEnabled() throws Exception {
    slf4j.logLevel("info");

    logger = new Slf4jLogger();
    logger.log(CONFIG_KEY, "A message with %d formatting %s.", 2, "tokens");
    logger.logRequest(CONFIG_KEY, Logger.Level.BASIC, REQUEST);
    logger.logAndRebufferResponse(CONFIG_KEY, Logger.Level.BASIC, RESPONSE, 273);
  }

  @Test
  public void logRequestsAndResponses() throws Exception {
    slf4j.logLevel("debug");
    slf4j.expectMessages("DEBUG feign.Logger - [someMethod] A message with 2 formatting tokens."
        + System.lineSeparator() +
        "DEBUG feign.Logger - [someMethod] ---> GET http://api.example.com HTTP/1.1"
        + System.lineSeparator() +
        "DEBUG feign.Logger - [someMethod] <--- HTTP/1.1 200 OK (273ms)"
        + System.lineSeparator());

    logger = new Slf4jLogger();
    logger.log(CONFIG_KEY, "A message with %d formatting %s.", 2, "tokens");
    logger.logRequest(CONFIG_KEY, Logger.Level.BASIC, REQUEST);
    logger.logAndRebufferResponse(CONFIG_KEY, Logger.Level.BASIC, RESPONSE, 273);
  }
}
