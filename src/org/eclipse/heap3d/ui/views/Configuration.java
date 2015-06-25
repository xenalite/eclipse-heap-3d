package org.eclipse.heap3d.ui.views;

import javafx.scene.input.KeyCode;

public class Configuration {

  public enum CollectionDisplayMode {
    ARRAY, OBJECT;
  }
  
  public enum StringDisplayMode {
    STRING, OBJECT;
  }
  
  public enum TypeNameDisplayMode {
    FULL, SHORT;
  }
  
  private static final Configuration INSTANCE = new Configuration();
  
  private Configuration() {}
  
  public static Configuration getInstance() {
    return INSTANCE;
  }
  
  public CollectionDisplayMode collectionDisplayMode = CollectionDisplayMode.ARRAY;

  public StringDisplayMode stringDisplayMode = StringDisplayMode.STRING;
  
  public TypeNameDisplayMode typeNameDisplayMode = TypeNameDisplayMode.SHORT;
  
  public KeyCode forwardsKey = KeyCode.W;
  
  public KeyCode backwardsKey = KeyCode.S;
  
  public KeyCode leftKey = KeyCode.A;
  
  public KeyCode rightKey = KeyCode.D;
  
  public KeyCode resetCameraKey = KeyCode.R;
  
  public KeyCode upKey = KeyCode.SPACE;
  
  public KeyCode downKey = KeyCode.C;
  
  public KeyCode speedUpKey = KeyCode.SHIFT;
  
}
