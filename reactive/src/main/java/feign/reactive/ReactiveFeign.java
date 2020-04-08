package feign.reactive;

import feign.Contract;
import feign.Feign;
import feign.InvocationHandlerFactory;

abstract class ReactiveFeign {



  public static class Builder extends Feign.Builder {

    private Contract contract = new Contract.Default();

    /**
     * Extend the current contract to support Reactive Stream return types.
     *
     * @param contract to extend.
     * @return a Builder for chaining.
     */
    @Override
    public Builder contract(Contract contract) {
      this.contract = contract;
      return this;
    }

    /**
     * Build the Feign instance.
     *
     * @return a new Feign Instance.
     */
    @Override
    public Feign build() {
      if (!(this.contract instanceof ReactiveDelegatingContract)) {
        super.contract(new ReactiveDelegatingContract(this.contract));
      } else {
        super.contract(this.contract);
      }
      return super.build();
    }

    @Override
    public Feign.Builder doNotCloseAfterDecode() {
      throw new UnsupportedOperationException("Streaming Decoding is not supported.");
    }
  }
}
