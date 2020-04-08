package feign.ribbon;

import com.netflix.client.ClientException;
import java.io.IOException;
import java.net.ConnectException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.hamcrest.CoreMatchers.isA;

public class PropagateFirstIOExceptionTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void propagatesNestedIOE() throws IOException {
    thrown.expect(IOException.class);

    RibbonClient.propagateFirstIOException(new ClientException(new IOException()));
  }

  @Test
  public void propagatesFirstNestedIOE() throws IOException {
    thrown.expect(IOException.class);
    thrown.expectCause(isA(IOException.class));

    RibbonClient.propagateFirstIOException(new ClientException(new IOException(new IOException())));
  }

  /**
   * Happened in practice; a blocking observable wrapped the connect exception in a runtime
   * exception
   */
  @Test
  public void propagatesDoubleNestedIOE() throws IOException {
    thrown.expect(ConnectException.class);

    RibbonClient.propagateFirstIOException(
        new ClientException(new RuntimeException(new ConnectException())));
  }

  @Test
  public void doesntPropagateWhenNotIOE() throws IOException {
    RibbonClient.propagateFirstIOException(
        new ClientException(new RuntimeException()));
  }
}
