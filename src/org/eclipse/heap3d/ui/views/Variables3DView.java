package org.eclipse.heap3d.ui.views;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class Variables3DView extends ViewPart {

  private FXCanvas canvas;
  
  @Override
  public void createPartControl(Composite parent) {
    canvas = new FXCanvas(parent, SWT.NONE);
    canvas.setScene(createScene());
  }

  private Scene createScene() {
    return null;
  }

  @Override
  public void setFocus() {
    canvas.setFocus();
  }

}