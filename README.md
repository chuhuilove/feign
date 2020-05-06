# Feign makes writing java http clients easier

[![Join the chat at https://gitter.im/OpenFeign/feign](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/OpenFeign/feign?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/OpenFeign/feign.svg?branch=master)](https://travis-ci.org/OpenFeign/feign)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.openfeign/feign-core/badge.png)](https://search.maven.org/artifact/io.github.openfeign/feign-core/)

Feign是受[Retrofit](https://github.com/square/retrofit), [JAXRS-2.0](https://jax-rs-spec.java.net/nonav/2.0/apidocs/index.html),和[WebSocket](http://www.oracle.com/technetwork/articles/java/jsr356-1937161.html).启发的Java到HTTP客户端绑定程序.Feign的第一个目标是减少与[ReSTfulness](http://www.slideshare.net/adrianfcole/99problems)无关的将[Denominator](https://github.com/Netflix/Denominator) 统一绑定到HTTP API的复杂性.
---
# 路线图
## Feign 11 and beyond 
Making _API_ clients easier

Short Term - What we're working on now. ⏰ 
---
* 响应缓存
  * 支持缓存api响应. 允许用户定义在什么条件下响应才适合进行缓存以及应使用哪种类型的缓存机制.
  * 支持内存中缓存和外部缓存实现 (EhCache, Google, Spring, etc...)
* 支持完整的URI模板表达式
  * 支持[level 1 through level 4](https://tools.ietf.org/html/rfc6570#section-1.2)URI模板表达式.
  * 使用[URI Templates TCK](https://github.com/uri-templates/uritemplate-test)验证合法性.
* `Logger` API重构
  * 重构`Logger`API,使之更接近SLF4J这样的框架,从而为在Feign中进行日志记录提供一个通用的智能模型.Feign本身将使用此模型,并为如何使用`Logger`提供更清晰的指导.
* `Retry` API重构
  * 重构`Retry` API以支持用户提供的条件并更好地控制回退策略.**这可能会导致不向后兼容的重大更改**

中期 - What's up next. ⏲ 
---
* Metric API
  * 提供一流的Metrics API,用户可以利用它来深入了解请求/响应生命周期.可能会提供更好的[OpenTracing](https://opentracing.io/)支持.
* 通过`CompletableFuture`支持异步执行
  * 在请求/响应生命周期中允许`Future`链接和执行者管理. **实现将需要非向后兼容的重大更改**. 但是,在考虑反应性执行之前,需要这个特性.
* Reactive execution support via [Reactive Streams](https://www.reactive-streams.org/)
  * 对于JDK 9+，请考虑使用`java.util.concurrent.Flow`的原生实现.
  * 支持JDK 8上的[Project Reactor](https://projectreactor.io/)和[RxJava 2+](https://github.com/ReactiveX/RxJava)实现.

长期 - The future ☁️ 
---
* 其他断路器支持
  * 支持其他断路器实现，例如[Resilience4J](https://resilience4j.readme.io/)和Spring Circuit断路器.

---  

### Why Feign and not X?

Feign使用Jersey和CXF之类的工具为ReST或SOAP服务编写Java客户端.此外,Feign允许您在诸如Apache HC之类的http库之上编写自己的代码.Feign通过可自定义的解码器和错误处理功能,以最小的开销和代码将代码连接到http API,可以将其写入任何基于文本的http API.

### How does Feign work?

Feign的工作原理是将注解处理成模板化的请求.在输出之前,参数以简单的方式应用于这些模板.尽管Feign仅限于支持基于文本的api,但它极大地简化了系统方面,如重试请求.

### Java Version Compatibility

Feign 10.x及更高版本是在Java 8上构建的,应在Java 9,10和11上运行.对于那些需要JDK 6兼容性的应用程序,请使用Feign 9.x

### Basics

使用通常看起来像这样,是[规范改造样本][canonical Retrofit sample](https://github.com/square/retrofit/blob/master/samples/src/main/java/com/example/retrofit/SimpleService.java)的改编.

```java
interface GitHub {
  @RequestLine("GET /repos/{owner}/{repo}/contributors")
  List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo);

  @RequestLine("POST /repos/{owner}/{repo}/issues")
  void createIssue(Issue issue, @Param("owner") String owner, @Param("repo") String repo);

}

public static class Contributor {
  String login;
  int contributions;
}

public static class Issue {
  String title;
  String body;
  List<String> assignees;
  int milestone;
  List<String> labels;
}

public class MyApp {
  public static void main(String... args) {
    GitHub github = Feign.builder()
                         .decoder(new GsonDecoder())
                         .target(GitHub.class, "https://api.github.com");
  
    // Fetch and print a list of the contributors to this library.
    List<Contributor> contributors = github.contributors("OpenFeign", "feign");
    for (Contributor contributor : contributors) {
      System.out.println(contributor.login + " (" + contributor.contributions + ")");
    }
  }
}
```

### 接口注解

Feign注解定义了接口与基础客户端应如何工作之间的`Contract`.Feign的默认contract定义了以下注解:

| Annotation     | Interface Target | Usage |
|----------------|------------------|-------|
| `@RequestLine` | Method           | Defines the `HttpMethod` and `UriTemplate` for request.  `Expressions`, values wrapped in curly-braces `{expression}` are resolved using their corresponding `@Param` annotated parameters. |
| `@Param`       | Parameter        | Defines a template variable, whose value will be used to resolve the corresponding template `Expression`, by name. |
| `@Headers`     | Method, Type     | Defines a `HeaderTemplate`; a variation on a `UriTemplate`.  that uses `@Param` annotated values to resolve the corresponding `Expressions`.  When used on a `Type`, the template will be applied to every request.  When used on a `Method`, the template will apply only to the annotated method. |
| `@QueryMap`    | Parameter        | Defines a `Map` of name-value pairs, or POJO, to expand into a query string. |
| `@HeaderMap`   | Parameter        | Defines a `Map` of name-value pairs, to expand into `Http Headers` |
| `@Body`        | Method           | Defines a `Template`, similar to a `UriTemplate` and `HeaderTemplate`, that uses `@Param` annotated values to resolve the corresponding `Expressions`.|


> **覆盖Request Line**
>
> 如果需要将请求定向到不同的主机,
> 则需要在创建Feign客户端时提供主机,或者要为每个请求提供目标主机,
> 请包含一个`java.net.URI`参数,Feign将使用该值作为请求目标.
>
> ```java
> @RequestLine("POST /repos/{owner}/{repo}/issues")
> void createIssue(URI host, Issue issue, @Param("owner") String owner, @Param("repo") String repo);
> ``` 
> 

### 模板和表达式

Feign `Expressions`表示简单字符串表达式(级别1),如[URI Template - RFC 6570](https://tools.ietf.org/html/rfc6570)所定义.`Expressions`使用对应的带注解的方法参数`Param`扩展.

*示例*

```java
public interface GitHub {
  
  @RequestLine("GET /repos/{owner}/{repo}/contributors")
  List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repository);
  
  class Contributor {
    String login;
    int contributions;
  }
}

public class MyApp {
  public static void main(String[] args) {
    GitHub github = Feign.builder()
                         .decoder(new GsonDecoder())
                         .target(GitHub.class, "https://api.github.com");
    
    /* The owner and repository parameters will be used to expand the owner and repo expressions
     * defined in the RequestLine.
     * 
     * the resulting uri will be https://api.github.com/repos/OpenFeign/feign/contributors
     */
    github.contributors("OpenFeign", "feign");
  }
}
```

表达式必须包含在大括号`{}`中,并且可以包含正则表达式模式,用冒号`:`分隔以进行限制解析值.*示例* `owner`必须是字母.`{owner:[a-zA-Z]*}`

#### 请求参数扩充

`RequestLine`和`QueryMap`模板遵循针对1级模板的[URI Template - RFC 6570](https://tools.ietf.org/html/rfc6570)规范,它规定了以下内容:

* 未解析的表达式将被省略.
* 如果还没有通过`@Param`注解进行编码或标记为`encoded`的话,所有文本和变量值都是pct编码的.


#### 未定义 vs. 空值 ####

未定义表达式是指表达式的值是显式的`null`或不提供值的表达式.
Per [URI Template - RFC 6570](https://tools.ietf.org/html/rfc6570), it is possible to provide an empty value
for an expression.  When Feign resolves an expression, it first determines if the value is defined, if it is then
the query parameter will remain.  If the expression is undefined, the query parameter is removed.  See below
for a complete breakdown.

*空字符串*
```java
public void test() {
   Map<String, Object> parameters = new LinkedHashMap<>();
   parameters.put("param", "");
   this.demoClient.test(parameters);
}
```
结果
```
http://localhost:8080/test?param=
```

*Missing*
```java
public void test() {
   Map<String, Object> parameters = new LinkedHashMap<>();
   this.demoClient.test(parameters);
}
```
结果
```
http://localhost:8080/test
```

*未定义*
```java
public void test() {
   Map<String, Object> parameters = new LinkedHashMap<>();
   parameters.put("param", null);
   this.demoClient.test(parameters);
}
```
结果
```
http://localhost:8080/test
```

查看[Advanced Usage](#advanced-usage)来获取更多示例.

> **那斜杠呢? `/`**
>
> `@RequestLine`和`@QueryMap`模板默认不对斜杠`/`编码.要更改此行为,请将`@RequestLine`上的`decodeSlash`属性设置为`false`.  

> *那加号呢? `+`**
>
> Per the URI specification, a `+` sign is allowed in both the path and query segments of a URI, however, handling of
> the symbol on the query can be inconsistent.  In some legacy systems, the `+` is equivalent to the a space.  Feign takes the approach of modern systems, where a
> `+` symbol should not represent a space and is explicitly encoded as `%2B` when found on a query string.
>
> If you wish to use `+` as a space, then use the literal ` ` character or encode the value directly as `%20`
 
##### 自定义扩展

`@Param`注解具有可选属性`expander`,可以完全控制各个参数的扩展.`expander`属性必须引用实现`Expander`接口的类.

```java
public interface Expander {
    String expand(Object value);
}
```
该方法的结果符合上述相同的规则. 如果结果为`null`或空字符串,则省略该值.如果该值未经过pct编码,则为.查看[Custom @Param Expansion](#custom-param-expansion)获取更多示例.

#### 请求头扩展 

`Headers`和`HeaderMap`模板遵循与[Request Parameter Expansion](#request-parameter-expansion)相同的规则,但有以下更改:

* 未解析的表达式将被省略. 如果结果是空的header值,则将删除整个header.
* 不执行pct编码.

查看[Headers](#headers)来看更多示例.

> **关于`@Param`参数及其名称的说明**: 
>
> 具有相同名称的所有表达式,无论它们在`@RequestLine`,`@QueryMap`,`@BodyTemplate`,还是`@Headers`上的位置如何,都将解析为相同的值.
> 在下面的示例中,将使用`contentType`的值来解析header和path表达式:
> ```java
> public interface ContentService {
>   @RequestLine("GET /api/documents/{contentType}")
>   @Headers("Accept: {contentType}")
>   String getDocumentByType(@Param("contentType") String type);
> }
>```
> 
> 设计接口时,请记住这一点.

#### 请求体扩展

`Body`模板遵循与[Request Parameter Expansion](#request-parameter-expansion)相同的规则,但有以下更改:

* 未解析的表达式被省略.
* 扩展值在放置在请求主体上之前**不**通过`Encoder`传递.
* 必须指定`Content-Type` header.看[Body Templates](#body-templates)示例.

---
### Customization定制

Feign有几个方面是可以定制的.对于简单的情况,你可以使用`Feign.builder()`来构造一个带有自定义组件的API接口.比如:

```java
interface Bank {
  @RequestLine("POST /account/{id}")
  Account getAccountInfo(@Param("id") String id);
}

public class BankService {
  public static void main(String[] args) {
    Bank bank = Feign.builder().decoder(
        new AccountDecoder())
        .target(Bank.class, "https://api.examplebank.com");
  }
}
```

### 多个接口
Feign可以生成多个api接口.这些被定义为`Target<T>`(默认`HardCodedTarget<T>`),它允许在执行之前动态发现和修饰请求.

例如,以下模式可能使用identity服务中的当前url和auth token 修饰每个请求.

```java
public class CloudService {
  public static void main(String[] args) {
    CloudDNS cloudDNS = Feign.builder()
      .target(new CloudIdentityTarget<CloudDNS>(user, apiKey));
  }
  
  class CloudIdentityTarget extends Target<CloudDNS> {
    /* implementation of a Target */
  }
}
```

### 示例
Feign包括示例[GitHub](./example-github)和[Wikipedia](./example-wikipedia)客户端.
denominator项目在实践中也可以取消.特别是,请查看其[example daemon](https://github.com/Netflix/denominator/tree/master/example-daemon).

---
### 集成
Feign可以与其他开放源代码工具一起很好地工作.

### Gson

[Gson](./gson) 包括可用于JSON API的编码器和解码器.

像这样将`GsonEncoder`和/或`GsonDecoder`添加到你的`Feign.Builder`中:

```java
public class Example {
  public static void main(String[] args) {
    GsonCodec codec = new GsonCodec();
    GitHub github = Feign.builder()
                         .encoder(new GsonEncoder())
                         .decoder(new GsonDecoder())
                         .target(GitHub.class, "https://api.github.com");
  }
}
```

### Jackson

[Jackson](./jackson) 包含可用于JSON API的编码器和解码器.

Add `JacksonEncoder` and/or `JacksonDecoder` to your `Feign.Builder` like so:

```java
public class Example {
  public static void main(String[] args) {
      GitHub github = Feign.builder()
                     .encoder(new JacksonEncoder())
                     .decoder(new JacksonDecoder())
                     .target(GitHub.class, "https://api.github.com");
  }
}
```

### Sax
[SaxDecoder](./sax) allows you to decode XML in a way that is compatible with normal JVM and also Android environments.

Here's an example of how to configure Sax response parsing:
```java
public class Example {
  public static void main(String[] args) {
      Api api = Feign.builder()
         .decoder(SAXDecoder.builder()
                            .registerContentHandler(UserIdHandler.class)
                            .build())
         .target(Api.class, "https://apihost");
    }
}
```

### JAXB
[JAXB](./jaxb) includes an encoder and decoder you can use with an XML API.

Add `JAXBEncoder` and/or `JAXBDecoder` to your `Feign.Builder` like so:

```java
public class Example {
  public static void main(String[] args) {
    Api api = Feign.builder()
             .encoder(new JAXBEncoder())
             .decoder(new JAXBDecoder())
             .target(Api.class, "https://apihost");
  }
}
```

### JAX-RS
[JAXRSContract](./jaxrs) overrides annotation processing to instead use standard ones supplied by the JAX-RS specification.  This is currently targeted at the 1.1 spec.

Here's the example above re-written to use JAX-RS:
```java
interface GitHub {
  @GET @Path("/repos/{owner}/{repo}/contributors")
  List<Contributor> contributors(@PathParam("owner") String owner, @PathParam("repo") String repo);
}

public class Example {
  public static void main(String[] args) {
    GitHub github = Feign.builder()
                       .contract(new JAXRSContract())
                       .target(GitHub.class, "https://api.github.com");
  }
}
```

### OkHttp
[OkHttpClient](./okhttp) directs Feign's http requests to [OkHttp](http://square.github.io/okhttp/), which enables SPDY and better network control.

To use OkHttp with Feign, add the OkHttp module to your classpath. Then, configure Feign to use the OkHttpClient:

```java
public class Example {
  public static void main(String[] args) {
    GitHub github = Feign.builder()
                     .client(new OkHttpClient())
                     .target(GitHub.class, "https://api.github.com");
  }
}
```

### Ribbon
[RibbonClient](./ribbon) overrides URL resolution of Feign's client, adding smart routing and resiliency capabilities provided by [Ribbon](https://github.com/Netflix/ribbon).

Integration requires you to pass your ribbon client name as the host part of the url, for example `myAppProd`.
```java
public class Example {
  public static void main(String[] args) {
    MyService api = Feign.builder()
          .client(RibbonClient.create())
          .target(MyService.class, "https://myAppProd");
  }
}
```

### Java 11 Http2
[Http2Client](./java11) directs Feign's http requests to Java11 [New HTTP/2 Client](http://www.javamagazine.mozaicreader.com/JulyAug2017#&pageSet=39&page=0) that implements HTTP/2.

To use New HTTP/2 Client with Feign, use Java SDK 11. Then, configure Feign to use the Http2Client:

```java
GitHub github = Feign.builder()
                     .client(new Http2Client())
                     .target(GitHub.class, "https://api.github.com");
```

### Hystrix
[HystrixFeign](./hystrix) configures circuit breaker support provided by [Hystrix](https://github.com/Netflix/Hystrix).

To use Hystrix with Feign, add the Hystrix module to your classpath. Then use the `HystrixFeign` builder:

```java
public class Example {
  public static void main(String[] args) {
    MyService api = HystrixFeign.builder().target(MyService.class, "https://myAppProd");
  }
}
```

### SOAP
[SOAP](./soap) includes an encoder and decoder you can use with an XML API.


This module adds support for encoding and decoding SOAP Body objects via JAXB and SOAPMessage. It also provides SOAPFault decoding capabilities by wrapping them into the original `javax.xml.ws.soap.SOAPFaultException`, so that you'll only need to catch `SOAPFaultException` in order to handle SOAPFault.

Add `SOAPEncoder` and/or `SOAPDecoder` to your `Feign.Builder` like so:

```java
public class Example {
  public static void main(String[] args) {
    Api api = Feign.builder()
	     .encoder(new SOAPEncoder(jaxbFactory))
	     .decoder(new SOAPDecoder(jaxbFactory))
	     .errorDecoder(new SOAPErrorDecoder())
	     .target(MyApi.class, "http://api");
  }
}
```

NB: you may also need to add `SOAPErrorDecoder` if SOAP Faults are returned in response with error http codes (4xx, 5xx, ...)

### SLF4J
[SLF4JModule](./slf4j) allows directing Feign's logging to [SLF4J](http://www.slf4j.org/), allowing you to easily use a logging backend of your choice (Logback, Log4J, etc.)

To use SLF4J with Feign, add both the SLF4J module and an SLF4J binding of your choice to your classpath.  Then, configure Feign to use the Slf4jLogger:

```java
public class Example {
  public static void main(String[] args) {
    GitHub github = Feign.builder()
                     .logger(new Slf4jLogger())
                     .target(GitHub.class, "https://api.github.com");
  }
}
```

### Decoders
`Feign.builder()` allows you to specify additional configuration such as how to decode a response.

If any methods in your interface return types besides `Response`, `String`, `byte[]` or `void`, you'll need to configure a non-default `Decoder`.

Here's how to configure JSON decoding (using the `feign-gson` extension):

```java
public class Example {
  public static void main(String[] args) {
    GitHub github = Feign.builder()
                     .decoder(new GsonDecoder())
                     .target(GitHub.class, "https://api.github.com");
  }
}
```

If you need to pre-process the response before give it to the Decoder, you can use the `mapAndDecode` builder method.
An example use case is dealing with an API that only serves jsonp, you will maybe need to unwrap the jsonp before
send it to the Json decoder of your choice:

```java
public class Example {
  public static void main(String[] args) {
    JsonpApi jsonpApi = Feign.builder()
                         .mapAndDecode((response, type) -> jsopUnwrap(response, type), new GsonDecoder())
                         .target(JsonpApi.class, "https://some-jsonp-api.com");
  }
}
```

### Encoders
The simplest way to send a request body to a server is to define a `POST` method that has a `String` or `byte[]` parameter without any annotations on it. You will likely need to add a `Content-Type` header.

```java
interface LoginClient {
  @RequestLine("POST /")
  @Headers("Content-Type: application/json")
  void login(String content);
}

public class Example {
  public static void main(String[] args) {
    client.login("{\"user_name\": \"denominator\", \"password\": \"secret\"}");
  }
}
```

By configuring an `Encoder`, you can send a type-safe request body. Here's an example using the `feign-gson` extension:

```java
static class Credentials {
  final String user_name;
  final String password;

  Credentials(String user_name, String password) {
    this.user_name = user_name;
    this.password = password;
  }
}

interface LoginClient {
  @RequestLine("POST /")
  void login(Credentials creds);
}

public class Example {
  public static void main(String[] args) {
    LoginClient client = Feign.builder()
                              .encoder(new GsonEncoder())
                              .target(LoginClient.class, "https://foo.com");
    
    client.login(new Credentials("denominator", "secret"));
  }
}
```

### @Body templates
The `@Body` annotation indicates a template to expand using parameters annotated with `@Param`. You will likely need to add a `Content-Type` header.

```java
interface LoginClient {

  @RequestLine("POST /")
  @Headers("Content-Type: application/xml")
  @Body("<login \"user_name\"=\"{user_name}\" \"password\"=\"{password}\"/>")
  void xml(@Param("user_name") String user, @Param("password") String password);

  @RequestLine("POST /")
  @Headers("Content-Type: application/json")
  // json curly braces must be escaped!
  @Body("%7B\"user_name\": \"{user_name}\", \"password\": \"{password}\"%7D")
  void json(@Param("user_name") String user, @Param("password") String password);
}

public class Example {
  public static void main(String[] args) {
    client.xml("denominator", "secret"); // <login "user_name"="denominator" "password"="secret"/>
    client.json("denominator", "secret"); // {"user_name": "denominator", "password": "secret"}
  }
}
```

### Headers
Feign supports settings headers on requests either as part of the api or as part of the client
depending on the use case.

#### Set headers using apis
In cases where specific interfaces or calls should always have certain header values set, it
makes sense to define headers as part of the api.

Static headers can be set on an api interface or method using the `@Headers` annotation.

```java
@Headers("Accept: application/json")
interface BaseApi<V> {
  @Headers("Content-Type: application/json")
  @RequestLine("PUT /api/{key}")
  void put(@Param("key") String key, V value);
}
```

Methods can specify dynamic content for static headers using variable expansion in `@Headers`.

```java
public interface Api {
   @RequestLine("POST /")
   @Headers("X-Ping: {token}")
   void post(@Param("token") String token);
}
```

In cases where both the header field keys and values are dynamic and the range of possible keys cannot
be known ahead of time and may vary between different method calls in the same api/client (e.g. custom
metadata header fields such as "x-amz-meta-\*" or "x-goog-meta-\*"), a Map parameter can be annotated
with `HeaderMap` to construct a query that uses the contents of the map as its header parameters.

```java
public interface Api {
   @RequestLine("POST /")
   void post(@HeaderMap Map<String, Object> headerMap);
}
```

These approaches specify header entries as part of the api and do not require any customizations
when building the Feign client.

#### Setting headers per target
In cases where headers should differ for the same api based on different endpoints or where per-request
customization is required, headers can be set as part of the client using a `RequestInterceptor` or a
`Target`.

For an example of setting headers using a `RequestInterceptor`, see the `Request Interceptors` section.

Headers can be set as part of a custom `Target`.

```java
  static class DynamicAuthTokenTarget<T> implements Target<T> {
    public DynamicAuthTokenTarget(Class<T> clazz,
                                  UrlAndTokenProvider provider,
                                  ThreadLocal<String> requestIdProvider);
    
    @Override
    public Request apply(RequestTemplate input) {
      TokenIdAndPublicURL urlAndToken = provider.get();
      if (input.url().indexOf("http") != 0) {
        input.insert(0, urlAndToken.publicURL);
      }
      input.header("X-Auth-Token", urlAndToken.tokenId);
      input.header("X-Request-ID", requestIdProvider.get());

      return input.request();
    }
  }
  
  public class Example {
    public static void main(String[] args) {
      Bank bank = Feign.builder()
              .target(new DynamicAuthTokenTarget(Bank.class, provider, requestIdProvider));
    }
  }
```

These approaches depend on the custom `RequestInterceptor` or `Target` being set on the Feign
client when it is built and can be used as a way to set headers on all api calls on a per-client
basis. This can be useful for doing things such as setting an authentication token in the header
of all api requests on a per-client basis. The methods are run when the api call is made on the
thread that invokes the api call, which allows the headers to be set dynamically at call time and
in a context-specific manner -- for example, thread-local storage can be used to set different
header values depending on the invoking thread, which can be useful for things such as setting
thread-specific trace identifiers for requests.

### Advanced usage

#### Base Apis
In many cases, apis for a service follow the same conventions. Feign supports this pattern via single-inheritance interfaces.

Consider the example:
```java
interface BaseAPI {
  @RequestLine("GET /health")
  String health();

  @RequestLine("GET /all")
  List<Entity> all();
}
```

You can define and target a specific api, inheriting the base methods.
```java
interface CustomAPI extends BaseAPI {
  @RequestLine("GET /custom")
  String custom();
}
```

In many cases, resource representations are also consistent. For this reason, type parameters are supported on the base api interface.

```java
@Headers("Accept: application/json")
interface BaseApi<V> {

  @RequestLine("GET /api/{key}")
  V get(@Param("key") String key);

  @RequestLine("GET /api")
  List<V> list();

  @Headers("Content-Type: application/json")
  @RequestLine("PUT /api/{key}")
  void put(@Param("key") String key, V value);
}

interface FooApi extends BaseApi<Foo> { }

interface BarApi extends BaseApi<Bar> { }
```

#### Logging
You can log the http messages going to and from the target by setting up a `Logger`.  Here's the easiest way to do that:
```java
public class Example {
  public static void main(String[] args) {
    GitHub github = Feign.builder()
                     .decoder(new GsonDecoder())
                     .logger(new Logger.JavaLogger("GitHub.Logger").appendToFile("logs/http.log"))
                     .logLevel(Logger.Level.FULL)
                     .target(GitHub.class, "https://api.github.com");
  }
}
```

> **A Note on JavaLogger**: 
> Avoid using of default ```JavaLogger()``` constructor - it was marked as deprecated and will be removed soon.

The SLF4JLogger (see above) may also be of interest.


#### 请求拦截器
When you need to change all requests, regardless of their target, you'll want to configure a `RequestInterceptor`.
For example, if you are acting as an intermediary, you might want to propagate the `X-Forwarded-For` header.

```java
static class ForwardedForInterceptor implements RequestInterceptor {
  @Override public void apply(RequestTemplate template) {
    template.header("X-Forwarded-For", "origin.host.com");
  }
}

public class Example {
  public static void main(String[] args) {
    Bank bank = Feign.builder()
                 .decoder(accountDecoder)
                 .requestInterceptor(new ForwardedForInterceptor())
                 .target(Bank.class, "https://api.examplebank.com");
  }
}
```

Another common example of an interceptor would be authentication, such as using the built-in `BasicAuthRequestInterceptor`.

```java
public class Example {
  public static void main(String[] args) {
    Bank bank = Feign.builder()
                 .decoder(accountDecoder)
                 .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                 .target(Bank.class, "https://api.examplebank.com");
  }
}
```

#### Custom @Param Expansion
Parameters annotated with `Param` expand based on their `toString`. By
specifying a custom `Param.Expander`, users can control this behavior,
for example formatting dates.

```java
public interface Api {
  @RequestLine("GET /?since={date}") Result list(@Param(value = "date", expander = DateToMillis.class) Date date);
}
```

#### 动态查询参数
可以使用`QueryMap`注解Map参数,以构造一个使用Map的内容作为其查询参数的查询.

```java
public interface Api {
  @RequestLine("GET /find")
  V find(@QueryMap Map<String, Object> queryMap);
}
```

也可以使用`QueryMapEncoder`从POJO对象生成查询参数.

```java
public interface Api {
  @RequestLine("GET /find")
  V find(@QueryMap CustomPojo customPojo);
}
```

当以这种方式使用时,无需指定自定义`QueryMapEncoder`,查询映射将使用成员变量名称作为查询参数名称来生成.
以下POJO将生成查询参数"/find?name={name}&number={number}"(不保证所包含查询参数的顺序,如果任何值为null,它将被忽略).

```java
public class CustomPojo {
  private final String name;
  private final int number;

  public CustomPojo (String name, int number) {
    this.name = name;
    this.number = number;
  }
}
```

设置自定义`QueryMapEncoder`:

```java
public class Example {
  public static void main(String[] args) {
    MyApi myApi = Feign.builder()
                 .queryMapEncoder(new MyCustomQueryMapEncoder())
                 .target(MyApi.class, "https://api.hostname.com");
  }
}
```

使用@QueryMap注解对象时,默认编码器使用反射检查提供的对象字段,以将对象值扩展为查询字符串.
如果您希望使用Java Beans API中定义的getter和setter方法构建查询字符串,请使用BeanQueryMapEncoder.

```java
public class Example {
  public static void main(String[] args) {
    MyApi myApi = Feign.builder()
                 .queryMapEncoder(new BeanQueryMapEncoder())
                 .target(MyApi.class, "https://api.hostname.com");
  }
}
```

### Error Handling
如果需要更多的控制来处理意外的响应,Feign实例可以通过构建器注册一个自定义的`ErrorDecoder`.

```java
public class Example {
  public static void main(String[] args) {
    MyApi myApi = Feign.builder()
                 .errorDecoder(new MyErrorDecoder())
                 .target(MyApi.class, "https://api.hostname.com");
  }
}
```

所有导致HTTP状态不在2xx范围的响应都将触发`ErrorDecoder`的`decode`方法,允许自定义处理响应、将故障包装成自定义异常或执行任何附加处理.
如果你想再次重试请求,抛出一个`RetryableException`.这将调用已注册的`Retryer`.

### Retry

默认情况下,Feign会自动重试`IOException`,而不考虑HTTP方法,将其视为与网络临时相关的异常,以及从`ErrorDecoder`抛出的任何`RetryableException`.
要自定义此行为,请通过生成器注册一个自定义`Retryer`实例.

```java
public class Example {
  public static void main(String[] args) {
    MyApi myApi = Feign.builder()
                 .retryer(new MyRetryer())
                 .target(MyApi.class, "https://api.hostname.com");
  }
}
```

`Retryer`负责通过从方法`continueOrPropagate(RetryableException e);`返回`true`或`false`来确定重试是否发生;
`Retryer`实例将为每个`Client`执行创建,允许您在需要时维护每个请求的状态.

如果确定重试不成功,将抛出最后一个`RetryException`.
要抛出导致不成功重试的原始原因,请使用`exceptionPropagationPolicy()`选项构建自己的Feign客户端.

#### 静态和默认方法
如果使用Java8+,则Feign的目标接口可能有静态或默认方法. 这些允许Feign客户端包含底层API没有明确定义的逻辑.
例如,静态方法使指定通用客户端构建配置变得容易;默认方法可用于组成查询或定义默认参数.

```java
interface GitHub {
  @RequestLine("GET /repos/{owner}/{repo}/contributors")
  List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo);

  @RequestLine("GET /users/{username}/repos?sort={sort}")
  List<Repo> repos(@Param("username") String owner, @Param("sort") String sort);

  default List<Repo> repos(String owner) {
    return repos(owner, "full_name");
  }

  /**
   * Lists all contributors for all repos owned by a user.
   */
  default List<Contributor> contributors(String user) {
    MergingContributorList contributors = new MergingContributorList();
    for(Repo repo : this.repos(owner)) {
      contributors.addAll(this.contributors(user, repo.getName()));
    }
    return contributors.mergeResult();
  }

  static GitHub connect() {
    return Feign.builder()
                .decoder(new GsonDecoder())
                .target(GitHub.class, "https://api.github.com");
  }
}
```
