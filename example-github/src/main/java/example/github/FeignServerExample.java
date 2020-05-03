package example.github;

import feign.Feign;
import feign.Logger;
import feign.Param;
import feign.RequestLine;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import java.util.UUID;

/**
 * @AUTHOR: cyzi
 * @DATE: 2020/4/30
 * @DESCRIPTION: todo
 */
public class FeignServerExample {



  interface FeignServer {


    class CusClass {

      private String name;
      private Integer age;

      @Override
      public String toString() {
        return "CusClass{" +
            "name='" + name + '\'' +
            ", age=" + age +
            '}';
      }

      public String getName() {
        return name;
      }

      public void setName(String name) {
        this.name = name;
      }

      public Integer getAge() {
        return age;
      }

      public void setAge(Integer age) {
        this.age = age;
      }
    }

    @RequestLine("GET /example/uuid")
    String getUUID();

    @RequestLine("POST /example/reqPost")
    CusClass reqPost();

    @RequestLine("POST /example/testPostRequest")
    CusClass testPostRequest(@Param("originalParam") CusClass originalParam);


    static FeignServer connect() {
      Decoder decoder = new GsonDecoder();
      Encoder encoder = new GsonEncoder();
      return Feign.builder()
          .encoder(encoder) // 设置编码器
          .decoder(decoder) // 设置解码器
          .logger(new Logger.ErrorLogger())
          .logLevel(Logger.Level.BASIC)
          .target(FeignServer.class, "http://localhost:8081/feign-server/");
    }

  }

  public static void main(String[] args) {

    FeignServer feignServer = FeignServer.connect();

    System.err.println(feignServer.getUUID());
    System.err.println(feignServer.reqPost());

    FeignServer.CusClass cusClass = new FeignServer.CusClass();

    cusClass.setAge(25);
    cusClass.setName("匆匆过客-张玉");
    System.err.println(feignServer.testPostRequest(cusClass));


    System.err.println(feignServer.getUUID());
    System.err.println(feignServer.reqPost());
    System.err.println(feignServer.testPostRequest(cusClass));

    System.err.println(feignServer.getUUID());
    System.err.println(feignServer.reqPost());
    System.err.println(feignServer.testPostRequest(cusClass));

    System.err.println(feignServer.getUUID());
    System.err.println(feignServer.reqPost());
    System.err.println(feignServer.testPostRequest(cusClass));
    System.err.println(feignServer.getUUID());
    System.err.println(feignServer.reqPost());
    System.err.println(feignServer.testPostRequest(cusClass));
    System.err.println(feignServer.getUUID());
    System.err.println(feignServer.reqPost());
    System.err.println(feignServer.testPostRequest(cusClass));
    System.err.println(feignServer.getUUID());
    System.err.println(feignServer.reqPost());
    System.err.println(feignServer.testPostRequest(cusClass));
    System.err.println(feignServer.getUUID());
    System.err.println(feignServer.reqPost());
    System.err.println(feignServer.testPostRequest(cusClass));



  }

}
