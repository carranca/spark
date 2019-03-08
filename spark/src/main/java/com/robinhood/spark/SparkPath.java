package com.robinhood.spark;

import android.graphics.Path;
import android.support.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class SparkPath {
  public final List<SparkPathSegment> segments = new LinkedList<>();
  @Nullable private SparkPathSegment currentSegment = null;
  private final SparkViewModel.SparkPathType pathType;

  SparkPath(SparkViewModel.SparkPathType pathType) {
    this.pathType = pathType;
  }

  SparkPath(SparkPath source) {
    this(source.pathType);

    SparkPathSegment newCurrentSegment = null;
    for (SparkPathSegment sourceSegment : source.segments) {
      final SparkPathSegment newSegment = new SparkPathSegment(sourceSegment);
      segments.add(newSegment);
      if (sourceSegment.equals(source.currentSegment)) {
        newCurrentSegment = newSegment;
      }
    }

    currentSegment = newCurrentSegment;
  }

  void startSegment(float x, float y) {
    if (currentSegment != null) {
      throw new IllegalStateException("trying to start segment but a segment already exists");
    }

    currentSegment = new SparkPathSegment(pathType, segments.size());
    currentSegment.moveTo(x, y);
  }

  void endSegment(@Nullable Float fillEdge, int startPadding) {
    if (currentSegment == null) {
      throw new IllegalStateException("trying to end segment, but no segment exists");
    }

    currentSegment.fillAndClose(fillEdge, startPadding);
    segments.add(currentSegment);
    currentSegment = null;
  }

  void addPointToSegment(float x, float y) {
    if (currentSegment == null) {
      throw new IllegalStateException("no segment to add to");
    }

    currentSegment.lineTo(x, y);
  }

  public void reset() {
    for (SparkPathSegment segment : segments) {
      segment.reset();
    }

    segments.clear();

    if (currentSegment != null) {
      currentSegment.reset();
    }
  }

  public static class SparkPathSegment extends Path {
    public final List<Float> xPoints = new LinkedList<>();
    public final List<Float> yPoints = new LinkedList<>();
    final SparkViewModel.SparkPathType pathType;
    final int indexInSparkPath;

    SparkPathSegment(SparkViewModel.SparkPathType pathType, int indexInSparkPath) {
      this.pathType = pathType;
      this.indexInSparkPath = indexInSparkPath;
    }

    SparkPathSegment(SparkPathSegment source) {
      super(source);
      this.pathType = source.pathType;
      this.indexInSparkPath = source.indexInSparkPath;
      this.xPoints.addAll(source.xPoints);
      this.yPoints.addAll(source.yPoints);
    }

    @Override public void moveTo(float x, float y) {
      super.moveTo(x, y);

      xPoints.add(x);
      yPoints.add(y);
    }

    @Override public void lineTo(float x, float y) {
      super.lineTo(x, y);

      xPoints.add(x);
      yPoints.add(y);
    }

    void fillAndClose(@Nullable Float fillEdge, int startPadding) {
      // if we're filling the graph in, close the path's circuit
      if (fillEdge != null && !xPoints.isEmpty()) {
        final float firstX = xPoints.get(0);
        final float lastX = xPoints.get(xPoints.size() - 1);
        // line up or down to the fill edge
        lineTo(lastX, fillEdge);
        // line straight left to far edge of the path
        lineTo(startPadding + firstX, fillEdge);

        // closes line back on the first point
        close();
      }
    }

    @Override public void reset() {
      super.reset();

      xPoints.clear();
      yPoints.clear();
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SparkPathSegment that = (SparkPathSegment) o;

      if (indexInSparkPath != that.indexInSparkPath) return false;
      return pathType.equals(that.pathType);
    }

    @Override public int hashCode() {
      int result = pathType.hashCode();
      result = 31 * result + indexInSparkPath;
      return result;
    }

    @Override public String toString() {
      return "SparkPathSegment{" +
          "xPoints=" + xPoints.size() +
          ", yPoints=" + yPoints.size() +
          ", pathType=" + pathType.getClass().getSimpleName() +
          ", indexInSparkPath=" + indexInSparkPath +
          '}';
    }
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SparkPath sparkPath = (SparkPath) o;

    return pathType.equals(sparkPath.pathType);
  }

  @Override public int hashCode() {
    return pathType.hashCode();
  }
}
