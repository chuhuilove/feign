package feign.jaxrs.examples;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import feign.Feign;
import feign.jaxrs.JAXRSContract;

/**
 * adapted from {@code com.example.retrofit.GitHubClient}
 */
public class GitHubExample {

  public static void main(String... args) throws InterruptedException {
    GitHub github = Feign.builder()
        .contract(new JAXRSContract())
        .target(GitHub.class, "https://api.github.com");

    System.out.println("Let's fetch and print a list of the contributors to this library.");
    List<Contributor> contributors = github.contributors("netflix", "feign");
    for (Contributor contributor : contributors) {
      System.out.println(contributor.login + " (" + contributor.contributions + ")");
    }
  }

  interface GitHub {

    @GET
    @Path("/repos/{owner}/{repo}/contributors")
    List<Contributor> contributors(@PathParam("owner") String owner,
                                   @PathParam("repo") String repo);
  }

  static class Contributor {

    String login;
    int contributions;
  }
}
