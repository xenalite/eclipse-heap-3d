package org.eclipse.heap3d.utils;

import javafx.beans.property.BooleanProperty;

public interface ICommand {

  void run();
  
  BooleanProperty isEnabled();  
}
