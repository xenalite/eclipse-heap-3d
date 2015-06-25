package org.eclipse.heap3d.ui.views;

import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import org.eclipse.heap3d.resources.Resources;
import org.eclipse.heap3d.utils.GeometryUtils;
import org.imperial.eclipse3dheap.reference.EdgeType;
import org.imperial.eclipse3dheap.reference.Reference;
import org.imperial.eclipse3dheap.reference.ReferenceAttributes;

public class ShapeFactory {

  private static final int FONT_SIZE = 140;
  private static final double ROOT_NODE_DIMENSIONS = 100;
  private static final double NON_ROOT_NODE_DIMENSIONS = 80;
  private static final double EDGE_WIDTH = 5;
  private static final double ARROW_SCALE = 30;

  private ColourPicker colourPicker = new ColourPicker();
  private InputHandler inputHandler;

  public ShapeFactory(InputHandler inputHandler) {
    this.inputHandler = inputHandler;
  }

  public Box createRootNodeBox(Point3D position) {
    PhongMaterial pm = new PhongMaterial();
    pm.setDiffuseColor(colourPicker.getNextPalette2());
    pm.setSpecularColor(Color.WHITE);

    Box box = createBox(ROOT_NODE_DIMENSIONS, pm);
    GeometryUtils.setTranslate(box, position);
    return box;
  }
  
  public BasicTransforms createRootText(String name, Point3D midpoint) {
    Group textGroup = new Group();
    BasicTransforms labelTransforms = new BasicTransforms();
    Text text = new Text(name);
    text.setFont(Font.font("Arial", FONT_SIZE));
    Bounds bounds = text.getLayoutBounds();

    textGroup.getChildren().add(text);
    textGroup.getTransforms().addAll(new Rotate(180, Rotate.Z_AXIS),
        new Translate(- bounds.getWidth() / 2D, 0, - NON_ROOT_NODE_DIMENSIONS * Math.sqrt(2)));
    
    labelTransforms.setTranslate(midpoint);
    labelTransforms.getChildren().addAll(textGroup);
    inputHandler.getLabels().add(labelTransforms);
    return labelTransforms;
  }

  public Shape3D createChildNodeBox(boolean isCollection, String typeName, Point3D position) {
    PhongMaterial pm = new PhongMaterial();
    pm.setDiffuseColor(colourPicker.getTypeColor(typeName));

    if(isCollection) {
      Sphere sphere = new Sphere(NON_ROOT_NODE_DIMENSIONS * 0.8D);
      sphere.setMaterial(pm);
      return sphere;
    }
    Box box = createBox(NON_ROOT_NODE_DIMENSIONS, pm);
    GeometryUtils.setTranslate(box, position);
    return box;
  }

  private MeshView createArrow() {
    float[] vertices = new float[] { -0.5f, -1.5f, -0.5f, 0.5f, -1.5f, -0.5f, 0.5f, -1.5f, 0.5f, -0.5f, -1.5f, 0.5f, 0,
        0, 0 };

    float[] texCoords = { 0, 0 };

    int[] faces = { 1, 0, 4, 0, 2, 0, 2, 0, 4, 0, 3, 0, 3, 0, 4, 0, 0, 0, 0, 0, 4, 0, 1, 0, 2, 0, 3, 0, 0, 0, 1, 0, 2,
        0, 0, 0 };

    TriangleMesh mesh = new TriangleMesh();
    mesh.getPoints().setAll(vertices);
    mesh.getTexCoords().setAll(texCoords);
    mesh.getFaces().setAll(faces);

    MeshView mv = new MeshView(mesh);
    return mv;
  }

  private BasicTransforms createText(Reference reference, Point3D midpoint) {
    Group textGroup = new Group();
    BasicTransforms labelTransforms = new BasicTransforms();
    String name = reference.getAttributes().size() > 1 ? "(...)" : reference.getAttributes().get(0).name;
    Text text = new Text(name);
    text.setFont(Font.font("Arial", FONT_SIZE));
    Bounds bounds = text.getLayoutBounds();

    ImageView imageView = getEdgeImage(reference);
    imageView.setTranslateY(- 2.5 * bounds.getHeight() / 4D);
    imageView.setTranslateX(- 150);
    imageView.getTransforms().add(new Scale(6,6,6));
    
    textGroup.getChildren().addAll(text, imageView);
    textGroup.getTransforms().addAll(new Rotate(180, Rotate.Z_AXIS),
        new Translate(- bounds.getWidth() / 2D, 0));
    
    labelTransforms.setTranslate(midpoint);
    labelTransforms.getChildren().addAll(textGroup);
    inputHandler.getLabels().add(labelTransforms);
    return labelTransforms;
  }
  
  public EdgeGroup createEdge(Point3D from, Point3D to, Reference reference) {
    Rotate ry = new Rotate(0, Rotate.Y_AXIS);
    Rotate rz = new Rotate(0, Rotate.Z_AXIS);

    Point3D direction2D = new Point3D(to.getX(), 0, to.getZ()).subtract(new Point3D(from.getX(), 0, from.getZ()));

    ry.setAngle((from.getZ() > to.getZ() ? 1 : -1)
        * (direction2D.equals(Point3D.ZERO) ? 0 : direction2D.angle(Rotate.X_AXIS)));

    rz.setAngle((from.getY() > to.getY() ? -1 : 1)
        * (direction2D.equals(Point3D.ZERO) ? 90 : to.subtract(from).angle(direction2D)));

    EdgeGroup group;
    if(reference.getEdgeType() == EdgeType.SINGLE)
      group = createStraightEdge(from, to, ry, rz, reference);
    else if(reference.getEdgeType() == EdgeType.LOOP)
      group = createLoopedEdge(from, reference);
    else if(reference.getEdgeType() == EdgeType.PARALLEL)
      group = createCurvedEdge(from, to, ry, rz, reference);
    else
      group = new EdgeGroup();
    return group;
  }
  
  private EdgeGroup createStraightEdge(Point3D from, Point3D to, Rotate ry, Rotate rz, Reference reference) {
    Point3D midpoint = from.midpoint(to);
    Box edge = createBox(from.distance(to), EDGE_WIDTH, EDGE_WIDTH, createEdgeMaterial(reference));
    GeometryUtils.setTranslate(edge, midpoint);
    edge.getTransforms().addAll(ry, rz);

    MeshView arrow = createArrow();
    GeometryUtils.setTranslate(arrow, from.multiply(0.2).add(to.multiply(0.8)));
    arrow.getTransforms().addAll(ry, rz, new Rotate(90, Rotate.X_AXIS), 
        new Rotate(270, Rotate.Z_AXIS), new Scale(ARROW_SCALE, ARROW_SCALE, ARROW_SCALE));

    EdgeGroup group = new EdgeGroup();
    BasicTransforms text = createText(reference, midpoint);
    group.getChildren().addAll(edge, arrow, text);
    return group;
  }

  private EdgeGroup createCurvedEdge(Point3D from, Point3D to, Rotate ry, Rotate rz, Reference reference) {
    Point3D t = to.subtract(from);
    Point3D tp = new Point3D(t.magnitude(), 0, 0);
    double w = t.magnitude();
    double m = w / 2;
    double halfWidth = 100;
    double modifier = 0.7D;

    Point3D a = new Point3D(m, 0, -halfWidth);
    Point3D b = new Point3D(m - w / 6, 0, -halfWidth);
    Point3D c = new Point3D(w / 6, 0, -modifier * halfWidth);
    Point3D bp = new Point3D(m + w / 6, 0, -halfWidth);
    Point3D cp = new Point3D(w - w / 6, 0, -modifier * halfWidth);

    double l1 = w / 3;
    double l2 = c.magnitude();
    double l3 = b.distance(c);

    Box f1 = createBox(l1, EDGE_WIDTH, EDGE_WIDTH, createEdgeMaterial(reference));
    Box f2 = createBox(l2, EDGE_WIDTH, EDGE_WIDTH, createEdgeMaterial(reference));
    Box f3 = createBox(l2, EDGE_WIDTH, EDGE_WIDTH, createEdgeMaterial(reference));
    Box f4 = createBox(l3, EDGE_WIDTH, EDGE_WIDTH, createEdgeMaterial(reference));
    Box f5 = createBox(l3, EDGE_WIDTH, EDGE_WIDTH, createEdgeMaterial(reference));

    double angle1 = - c.angle(Rotate.X_AXIS);
    double angle2 = - b.subtract(c).angle(Rotate.X_AXIS);

    GeometryUtils.setTranslate(f1, a);
    
    GeometryUtils.setTranslate(f2, c.midpoint(Point3D.ZERO));
    GeometryUtils.setRotate(f2, -angle1, Rotate.Y_AXIS);
    
    GeometryUtils.setTranslate(f3, cp.midpoint(tp));
    GeometryUtils.setRotate(f3, angle1, Rotate.Y_AXIS);
    
    GeometryUtils.setTranslate(f4, b.midpoint(c));
    GeometryUtils.setRotate(f4, -angle2, Rotate.Y_AXIS);
    
    GeometryUtils.setTranslate(f5, bp.midpoint(cp));
    GeometryUtils.setRotate(f5, angle2, Rotate.Y_AXIS);

    MeshView arrow = createArrow();
    GeometryUtils.setTranslate(arrow, cp.midpoint(tp));
    arrow.getTransforms().addAll(new Rotate(angle1, Rotate.Y_AXIS), new Rotate(90, Rotate.Y_AXIS), new Rotate(90, Rotate.X_AXIS),
        new Scale(ARROW_SCALE, ARROW_SCALE, ARROW_SCALE));
    
    BasicTransforms text = createText(reference, ry.transform(a).add(from));
    
    EdgeGroup overall = new EdgeGroup();
    Group connector = new Group();
    connector.getChildren().addAll(f1, f2, f3, f4, f5, arrow);
    connector.getTransforms().addAll(GeometryUtils.createTranslate(from), ry, rz);
    overall.getChildren().addAll(connector, text);
    return overall;
  }

  private EdgeGroup createLoopedEdge(Point3D from, Reference reference) {
    final double CORNER_LENGTH = NON_ROOT_NODE_DIMENSIONS * Math.sqrt(2) * 0.5D;
    final double SIDE_LENGTH = NON_ROOT_NODE_DIMENSIONS * 2;
    final double POINT_OFFSET = 0.5D * (SIDE_LENGTH + NON_ROOT_NODE_DIMENSIONS);
    final double HALF_SIDE_LENGTH = SIDE_LENGTH * 0.5D;

    Point3D topPoint = new Point3D(0, 0, POINT_OFFSET);
    Point3D bottomPoint = new Point3D(0, 0, -POINT_OFFSET);
    Point3D leftPoint = new Point3D(-POINT_OFFSET, 0, 0);
    Point3D rightPoint = new Point3D(POINT_OFFSET, 0, 0);
    
    Box top = createBox(SIDE_LENGTH, EDGE_WIDTH, EDGE_WIDTH, createEdgeMaterial(reference));
    Box bottom = createBox(SIDE_LENGTH, EDGE_WIDTH, EDGE_WIDTH, createEdgeMaterial(reference));
    Box left = createBox(EDGE_WIDTH, EDGE_WIDTH, SIDE_LENGTH, createEdgeMaterial(reference));
    Box right = createBox(EDGE_WIDTH, EDGE_WIDTH, SIDE_LENGTH, createEdgeMaterial(reference));
    
    Box topleft = createBox(CORNER_LENGTH, EDGE_WIDTH, EDGE_WIDTH, createEdgeMaterial(reference));
    Box topright = createBox(CORNER_LENGTH, EDGE_WIDTH, EDGE_WIDTH, createEdgeMaterial(reference));
    Box bottomleft = createBox(CORNER_LENGTH, EDGE_WIDTH, EDGE_WIDTH, createEdgeMaterial(reference));
    Box bottomright = createBox(CORNER_LENGTH, EDGE_WIDTH, EDGE_WIDTH, createEdgeMaterial(reference));

    GeometryUtils.setTranslate(top, topPoint);
    GeometryUtils.setTranslate(bottom, bottomPoint);
    GeometryUtils.setTranslate(left, leftPoint);
    GeometryUtils.setTranslate(right, rightPoint);

    GeometryUtils.setTranslate(topleft, 
        topPoint.subtract(new Point3D(HALF_SIDE_LENGTH, 0, 0))
          .midpoint(leftPoint.add(new Point3D(0, 0, HALF_SIDE_LENGTH))));

    GeometryUtils.setTranslate(topright, 
        topPoint.subtract(new Point3D(-HALF_SIDE_LENGTH, 0, 0))
          .midpoint(rightPoint.add(new Point3D(0, 0, HALF_SIDE_LENGTH))));
    
    GeometryUtils.setTranslate(bottomleft, 
        bottomPoint.subtract(new Point3D(HALF_SIDE_LENGTH, 0, 0))
          .midpoint(leftPoint.add(new Point3D(0, 0, -HALF_SIDE_LENGTH))));
    
    GeometryUtils.setTranslate(bottomright, 
        bottomPoint.subtract(new Point3D(-HALF_SIDE_LENGTH, 0, 0)).midpoint(
            rightPoint.add(new Point3D(0, 0, -HALF_SIDE_LENGTH))));
    
    GeometryUtils.setRotate(topleft, -45, Rotate.Y_AXIS);
    GeometryUtils.setRotate(topright, 45, Rotate.Y_AXIS);
    GeometryUtils.setRotate(bottomleft, 45, Rotate.Y_AXIS);
    GeometryUtils.setRotate(bottomright, -45, Rotate.Y_AXIS);
    
    MeshView arrow = createArrow();
    GeometryUtils.setTranslate(arrow, new Point3D(-HALF_SIDE_LENGTH, 0, -POINT_OFFSET));
    arrow.getTransforms().addAll(new Rotate(270, Rotate.Y_AXIS), new Rotate(90, Rotate.X_AXIS),
        new Scale(ARROW_SCALE, ARROW_SCALE, ARROW_SCALE));

    Rotate rotate = new Rotate(180, Rotate.Y_AXIS);
    Point3D t = from.subtract(new Point3D(POINT_OFFSET, 0, POINT_OFFSET));
    
    BasicTransforms text = createText(reference, rotate.transform(topPoint.subtract(new Point3D(-HALF_SIDE_LENGTH, 0, 0))
        .midpoint(rightPoint.add(new Point3D(0, 0, HALF_SIDE_LENGTH)))).add(t));

    EdgeGroup overall = new EdgeGroup();
    
    Group group = new Group();
    group.getChildren().addAll(top, bottom, left, right, topleft, topright, bottomright, arrow);
    
    group.getTransforms().addAll(GeometryUtils.createTranslate(t), rotate);
    overall.getChildren().addAll(group, text);
    return overall;
  }

  private PhongMaterial createEdgeMaterial(Reference reference) {
    PhongMaterial pm = new PhongMaterial();
    pm.setSpecularColor(Color.WHITE);
    Color edgeColor = Color.DARKGRAY;
    pm.setDiffuseColor(edgeColor);
    return pm;
  }
  
  private ImageView getEdgeImage(Reference reference) {
    if(reference.getAttributes().size() > 1)
      return new ImageView();
    else {
      ReferenceAttributes attributes = reference.getAttributes().get(0);
      Image image = null;
      if(attributes.isPrivate)
        image = Resources.PRIVATE_ICON;
      if(attributes.isPublic)
        image = Resources.PUBLIC_ICON;
      if(attributes.isProtected)
        image = Resources.PROTECTED_ICON;
      if(attributes.isPackage)
        image = Resources.PACKAGE_ICON;
      if(attributes.isLocal)
        image = Resources.LOCALVAR_ICON;
      return new ImageView(image);
    }
  }

  private Box createBox(double dimensions, Material m) {
    Box box = new Box(dimensions, dimensions, dimensions);
    box.setMaterial(m);
    return box;
  }

  private Box createBox(double w, double h, double l, Material m) {
    Box box = new Box(w, h, l);
    box.setMaterial(m);
    return box;
  }
}
