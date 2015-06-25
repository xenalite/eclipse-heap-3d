package org.eclipse.heap3d.utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Command implements ICommand {

  private BooleanProperty isEnabled = new SimpleBooleanProperty();
  private Runnable r;
  
  public Command(Runnable r) {
    isEnabled.set(true);
    this.r = r;
  }

  @Override
  public void run() {
    if(isEnabled.get())
      r.run();
  }

  @Override
  public BooleanProperty isEnabled() {
    return isEnabled;
  }
}
