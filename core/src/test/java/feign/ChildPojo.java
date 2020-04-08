package feign;

class ParentPojo {
  public String parentPublicProperty;
  protected String parentProtectedProperty;

  public String getParentPublicProperty() {
    return parentPublicProperty;
  }

  public void setParentPublicProperty(String parentPublicProperty) {
    this.parentPublicProperty = parentPublicProperty;
  }

  public String getParentProtectedProperty() {
    return parentProtectedProperty;
  }

  public void setParentProtectedProperty(String parentProtectedProperty) {
    this.parentProtectedProperty = parentProtectedProperty;
  }
}


public class ChildPojo extends ParentPojo {
  private String childPrivateProperty;

  public String getChildPrivateProperty() {
    return childPrivateProperty;
  }

  public void setChildPrivateProperty(String childPrivateProperty) {
    this.childPrivateProperty = childPrivateProperty;
  }
}
