package org.eclipse.heap3d.utils;

import java.util.Optional;

import javafx.geometry.Point3D;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class GeometryUtils {

  /*
   * 
   * 
   * 
   *  Intersection
   * 
   * 
   * 
   */
  
  public static Optional<Point3D> getIntersectionPoint(Point3D from, Point3D to, double destinationWidth) {
    Point3D minVertex = new Point3D(to.getX() - destinationWidth, to.getY() - destinationWidth, to.getZ()
        - destinationWidth);
    Point3D maxVertex = new Point3D(to.getX() + destinationWidth, to.getY() + destinationWidth, to.getZ()
        + destinationWidth);
    return getIntersectionPoint(minVertex, maxVertex, from, to);
  }

  private static Optional<Point3D> getIntersection(double d, double e, Point3D p0, Point3D p1) {
    if ((d * e) >= 0.0d)
      return Optional.empty();
    if (Double.compare(d, e) == 0)
      return Optional.empty();

    return Optional.of(p0.add(p1.subtract(p0).multiply((-d / (e - d)))));
  }

  private static boolean intersects(Point3D p0, Point3D p1, Optional<Point3D> intersection, Point3D axis) {
    if (!intersection.isPresent())
      return false;

    Point3D pIntersection = intersection.get();
    return (axis == Rotate.X_AXIS && pIntersection.getZ() >= p0.getZ() && pIntersection.getZ() <= p1.getZ()
        && pIntersection.getY() >= p0.getY() && pIntersection.getY() <= p1.getY() || axis == Rotate.Y_AXIS
        && pIntersection.getZ() >= p0.getZ() && pIntersection.getZ() <= p1.getZ() && pIntersection.getX() >= p0.getX()
        && pIntersection.getX() <= p1.getX() || axis == Rotate.Z_AXIS && pIntersection.getX() >= p0.getX()
        && pIntersection.getX() <= p1.getX() && pIntersection.getY() >= p0.getY() && pIntersection.getY() <= p1.getY());
  }

  public static Optional<Point3D> getIntersectionPoint(Point3D minVertex, Point3D maxVertex, Point3D from, Point3D to) {
    if ((to.getX() < minVertex.getX() && from.getX() < minVertex.getX())
        || (to.getX() > maxVertex.getX() && from.getX() > maxVertex.getX())
        || (to.getY() < minVertex.getY() && from.getY() < minVertex.getY())
        || (to.getY() > maxVertex.getY() && from.getY() > maxVertex.getY())
        || (to.getZ() < minVertex.getZ() && from.getZ() < minVertex.getZ())
        || (to.getZ() > maxVertex.getZ() && from.getZ() > maxVertex.getZ()))
      return Optional.empty();

    if (from.getX() > minVertex.getX() && from.getX() < maxVertex.getX() && from.getY() > minVertex.getY()
        && from.getY() < maxVertex.getY() && from.getZ() > minVertex.getZ() && from.getZ() < maxVertex.getZ())
      return Optional.of(from);

    Optional<Point3D> intersection = getIntersection(from.getX() - minVertex.getX(), to.getX() - minVertex.getX(),
        from, to);
    if (intersects(minVertex, maxVertex, intersection, Rotate.X_AXIS))
      return intersection;

    intersection = getIntersection(from.getY() - minVertex.getY(), to.getY() - minVertex.getY(), from, to);
    if (intersects(minVertex, maxVertex, intersection, Rotate.Y_AXIS))
      return intersection;

    intersection = getIntersection(from.getZ() - minVertex.getZ(), to.getZ() - minVertex.getZ(), from, to);
    if (intersects(minVertex, maxVertex, intersection, Rotate.Z_AXIS))
      return intersection;

    intersection = getIntersection(from.getX() - maxVertex.getX(), to.getX() - maxVertex.getX(), from, to);
    if (intersects(minVertex, maxVertex, intersection, Rotate.X_AXIS))
      return intersection;

    intersection = getIntersection(from.getY() - maxVertex.getY(), to.getY() - maxVertex.getY(), from, to);
    if (intersects(minVertex, maxVertex, intersection, Rotate.Y_AXIS))
      return intersection;

    intersection = getIntersection(from.getZ() - maxVertex.getZ(), to.getZ() - maxVertex.getZ(), from, to);
    if (intersects(minVertex, maxVertex, intersection, Rotate.Z_AXIS))
      return intersection;

    return Optional.empty();
  }
  
  /*
   * 
   * 
   * 
   *  Transforms
   * 
   * 
   * 
   */
  
  public static void setTranslate(Shape3D shape, Point3D point) {
    shape.setTranslateX(point.getX());
    shape.setTranslateY(point.getY());
    shape.setTranslateZ(point.getZ());
  }
  
  public static Translate createTranslate(Point3D point) {
    return new Translate(point.getX(), point.getY(), point.getZ());
  }
  
  public static void setRotate(Shape3D shape, double angle, Point3D axis) {
    shape.setRotationAxis(axis);
    shape.setRotate(angle);
  }

  public static void setTranslate(Translate t, Point3D point) {
    t.setX(point.getX());
    t.setY(point.getY());
    t.setZ(point.getZ());
  }
}
