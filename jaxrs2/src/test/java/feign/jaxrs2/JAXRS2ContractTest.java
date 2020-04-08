package feign.jaxrs2;

import feign.jaxrs.JAXRSContract;
import feign.jaxrs.JAXRSContractTest;

/**
 * Tests interfaces defined per {@link JAXRS2Contract} are interpreted into expected
 * {@link feign .RequestTemplate template} instances.
 */
public class JAXRS2ContractTest extends JAXRSContractTest {

  @Override
  protected JAXRSContract createContract() {
    return new JAXRS2Contract();
  }

}
