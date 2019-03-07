package com.robinhood.spark;

import android.graphics.Path;
import android.support.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class SparkPath extends Path {
  @Nullable private SparkPathSegment currentSegment = null;
  private final SparkViewModel.SparkPathType pathType;

  SparkPath(SparkViewModel.SparkPathType pathType) {
    this.pathType = pathType;
  }

  void startSegment(float x, float y) {
    if (currentSegment != null) {
      throw new IllegalStateException("trying to start segment but a segment already exists");
    }

    currentSegment = new SparkPathSegment();
    currentSegment.moveTo(x, y);
  }

  void endSegment(@Nullable Float fillEdge, int startPadding) {
    if (currentSegment == null) {
      throw new IllegalStateException("trying to end segment, but no segment exists");
    }

    currentSegment.fillAndClose(fillEdge, startPadding);
    addPath(currentSegment);
    currentSegment = null;
  }

  void addPointToSegment(float x, float y) {
    if (currentSegment == null) {
      throw new IllegalStateException("no segment to add to");
    }

    currentSegment.lineTo(x, y);
  }

  @Override public void reset() {
    super.reset();

    if (currentSegment != null) {
      currentSegment.reset();
    }
  }

  static class SparkPathSegment extends Path {
    final List<Float> xPoints = new LinkedList<>();
    final List<Float> yPoints = new LinkedList<>();

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
