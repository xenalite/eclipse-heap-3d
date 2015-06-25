package org.eclipse.heap3d.resources;

import javafx.scene.image.Image;

public class Resources {

  public static final Image PUBLIC_ICON = new Image(Resources.class.getResource("methpub_obj.gif").toString());

  public static final Image PROTECTED_ICON = new Image(Resources.class.getResource("methpro_obj.gif").toString());
  
  public static final Image PACKAGE_ICON = new Image(Resources.class.getResource("methdef_obj.gif").toString());
  
  public static final Image PRIVATE_ICON = new Image(Resources.class.getResource("methpri_obj.gif").toString());

  public static final Image LOCALVAR_ICON = new Image(Resources.class.getResource("localvariable_obj.gif").toString());

  public static final Image SPINNER = new Image(Resources.class.getResource("spinner.gif").toString());
  
  public static final Image WHITE_ILLUMINATION_MAP = new Image(Resources.class.getResource("white.png").toString());

  public static final Image INTERFACE_ICON = new Image(Resources.class.getResource("int_obj.gif").toString());
  
  public static final Image CLASS_ICON = new Image(Resources.class.getResource("class_obj.gif").toString());
  
  public static final Image BLOB = new Image(Resources.class.getResource("blob.png").toString());

  public static final Image SEARCH_ICON = new Image(Resources.class.getResource("insp_sbook.gif").toString());
}
