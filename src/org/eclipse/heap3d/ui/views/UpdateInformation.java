package org.eclipse.heap3d.ui.views;

import java.util.Map;
import java.util.Map.Entry;

import javafx.geometry.Point3D;

import org.eclipse.heap3d.graph.edges.Reference;
import org.eclipse.heap3d.graph.nodes.INode;

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
