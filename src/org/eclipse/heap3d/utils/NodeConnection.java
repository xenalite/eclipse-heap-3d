package org.eclipse.heap3d.utils;

import javafx.geometry.Point3D;

import org.imperial.eclipse3dheap.nodes.INode;
import org.imperial.eclipse3dheap.reference.Reference;

public class NodeConnection {

  public static final NodeConnection EMPTY = new NodeConnection(null, null, null, null, null);

  public final INode fromNode;
  public final INode toNode;

  public final Point3D fromPosition;
  public final Point3D toPosition;

  public final Reference edge;

  public NodeConnection(INode fromNode, Point3D fromPosition, INode toNode, Point3D toPosition, Reference edge) {

    this.fromNode = fromNode;
    this.toNode = toNode;

    this.fromPosition = fromPosition;
    this.toPosition = toPosition;

    this.edge = edge;
  }
}
