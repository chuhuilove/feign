package example.github;

import feign.Feign;
import feign.Logger;
import feign.RequestLine;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

/**
 * @AUTHOR: cyzi
 * @DATE: 2020/4/30
 * @DESCRIPTION: todo
 */
public class FeignServerExample {

    interface FeignServer{

        @RequestLine("GET /example/uuid")
        String getUUID();


        static  FeignServer connect() {
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

    }

}
