package org.eclipse.heap3d.graph.edges;

import java.util.ArrayList;
import java.util.List;

public class Reference {

  private EdgeType type = EdgeType.SINGLE;
  private List<ReferenceAttributes> attributes = new ArrayList<>();

  public Reference(ReferenceAttributes attribute) {
    attributes.add(attribute);
  }

  public List<ReferenceAttributes> getAttributes() {
    return attributes;
  }

  public EdgeType getEdgeType() {
    return type;
  }

  public void setEdgeType(EdgeType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return attributes.toString();
  }
}
