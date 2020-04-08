package feign;

public class PropertyPojo {

  private String name;

  public static class ChildPojoClass extends PropertyPojo {
    private Integer number;

    private String privateGetterProperty;

    public Integer getNumber() {
      return number;
    }

    public void setNumber(Integer number) {
      this.number = number;
    }

    public void setPrivateGetterProperty(String privateGetterProperty) {
      this.privateGetterProperty = privateGetterProperty;
    }

    private String getPrivateGetterProperty() {
      return privateGetterProperty;
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
