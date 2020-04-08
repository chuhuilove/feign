package feign.sax.examples;

import org.xml.sax.helpers.DefaultHandler;
import feign.Feign;
import feign.Request;
import feign.RequestLine;
import feign.RequestTemplate;
import feign.Target;
import feign.sax.SAXDecoder;

public class IAMExample {

  public static void main(String... args) {
    IAM iam = Feign.builder()//
        .decoder(SAXDecoder.builder().registerContentHandler(UserIdHandler.class).build())//
        .target(new IAMTarget(args[0], args[1]));
    System.out.println(iam.userId());
  }

  interface IAM {

    @RequestLine("GET /?Action=GetUser&Version=2010-05-08")
    Long userId();
  }

  static class IAMTarget extends AWSSignatureVersion4 implements Target<IAM> {

    private IAMTarget(String accessKey, String secretKey) {
      super(accessKey, secretKey);
    }

    @Override
    public Class<IAM> type() {
      return IAM.class;
    }

    @Override
    public String name() {
      return "iam";
    }

    @Override
    public String url() {
      return "https://iam.amazonaws.com";
    }

    @Override
    public Request apply(RequestTemplate in) {
      in.target(url());
      return super.apply(in);
    }
  }

  static class UserIdHandler extends DefaultHandler
      implements SAXDecoder.ContentHandlerWithResult<Long> {

    private StringBuilder currentText = new StringBuilder();

    private Long userId;

    @Override
    public Long result() {
      return userId;
    }

    @Override
    public void endElement(String uri, String name, String qName) {
      if (qName.equals("UserId")) {
        this.userId = Long.parseLong(currentText.toString().trim());
      }
      currentText = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) {
      currentText.append(ch, start, length);
    }
  }
}
