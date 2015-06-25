package org.eclipse.heap3d.ui.views;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;

public class ColourPicker {

  public static final Color IN_EDGE_COLOR = Color.RED;
  
  public static final Color OUT_EDGE_COLOR = Color.BLUE;
  
  public static final Color DEFAULT_EDGE_COLOR = Color.DARKGRAY;
  
  private Color[] palette1 = new Color[] { 
      Color.web("#F44336"),
      Color.web("#2196F3"),
      Color.web("#558B2F"),
      Color.web("#FFEB3B"),
      Color.web("#607D8B")
  };
  private int index1 = 0;
  
  private Color[] palette2 = new Color[] {
      Color.web("#9E9E9E"),
      Color.web("#E0E0E0"),
      Color.web("#616161")
  };
  private int index2 = 0;
  
  private Map<String, Color> typeColors = new HashMap<>();
  
  public Color getNextPalette1() {
    Color c = palette1[index1];
    index1 = ((index1 + 1) % palette1.length);
    return c;
  }
  
  public Color getNextPalette2() {
    Color c = palette2[index2];
    index2 = ((index2 + 1) % palette2.length);
    return c;
  }
  
  public Color getTypeColor(String type) {
	  if(!typeColors.containsKey(type))
		  typeColors.put(type, getNextPalette1());
	  return typeColors.get(type);
  }
}
