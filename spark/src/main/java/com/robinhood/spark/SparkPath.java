package com.robinhood.spark;

import android.graphics.Path;
import java.util.LinkedList;
import java.util.List;

class SparkPath extends Path {
  final int startIndex;
  final List<Float> xPoints = new LinkedList<>();
  final List<Float> yPoints = new LinkedList<>();
  final SparkViewModel.SparkPathType pathType;

  SparkPath(SparkViewModel.SparkPathType pathType, int startIndex) {
    this.pathType = pathType;
    this.startIndex = startIndex;
  }

  void addPoint(float x, float y) {
    xPoints.add(x);
    yPoints.add(y);
  }

  SparkPath fillAndClose(Float fillEdge, int startPadding) {
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

    return this;
  }

}
