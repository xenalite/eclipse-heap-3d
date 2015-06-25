package org.eclipse.heap3d.ui.views;

import java.util.Map;
import java.util.Map.Entry;

import org.imperial.eclipse3dheap.nodes.INode;
import org.imperial.eclipse3dheap.reference.Reference;

import javafx.geometry.Point3D;

public class UpdateInformation {

  public final INode parentNode;
  public final Point3D parentPosition;
  public final Map<Reference, Point3D> positionMap;
  public final Map<INode, Entry<Reference, Point3D>> reversePositionMap;
  
  public UpdateInformation(INode parentNode, Point3D parentPosition, 
      Map<Reference, Point3D> positionMap, Map<INode, Entry<Reference, Point3D>> reversePositionMap) {
    
    this.parentNode = parentNode;
    this.parentPosition = parentPosition;
    this.positionMap = positionMap;
    this.reversePositionMap = reversePositionMap;
  }
}
