package feign.example.github;

import static org.junit.Assert.assertThat;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import java.io.File;
import java.util.Arrays;

/**
 * Run main for {@link GitHubExampleIT}
 */
public class GitHubExampleIT {

  @Test
  public void runMain() throws Exception {
    final String jar = Arrays.stream(new File("target").listFiles())
        .filter(file -> file.getName().startsWith("feign-example-github")
            && file.getName().endsWith(".jar"))
        .findFirst()
        .map(File::getAbsolutePath)
        .get();

    final String line = "java -jar " + jar;
    final CommandLine cmdLine = CommandLine.parse(line);
    final int exitValue = new DefaultExecutor().execute(cmdLine);

    assertThat(exitValue, CoreMatchers.equalTo(0));
  }

}
