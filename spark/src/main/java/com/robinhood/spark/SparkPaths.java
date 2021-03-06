package com.robinhood.spark;

import android.support.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SparkPaths {
  public final Map<SparkPathType, SparkPath> paths = new HashMap<>();

  SparkPaths() {

  }

  SparkPaths(SparkPaths source) {
    for (SparkPathType pathType : source.paths.keySet()) {
      paths.put(pathType, new SparkPath(source.paths.get(pathType)));
    }
  }

  void startPathSegment(SparkPathType pathType, float x, float y) {
    SparkPath sparkPath = paths.get(pathType);
    if (sparkPath == null) {
      sparkPath = new SparkPath(pathType);
      paths.put(pathType, sparkPath);
    }

    sparkPath.startSegment(x, y);
  }

  void endPathSegment(
      SparkPathType pathType,
      @Nullable Float fillEdge,
      int startPadding
  ) {
    SparkPath sparkPath = paths.get(pathType);
    if (sparkPath == null) {
      throw new IllegalStateException("Trying to end path segment, but no such path exists");
    }

    sparkPath.endSegment(fillEdge, startPadding);
  }

  void addToPathSegment(SparkPathType pathType, float x, float y) {
    SparkPath sparkPath = paths.get(pathType);
    if (sparkPath == null) {
      throw new IllegalStateException("Trying to add to path segment, but no such path exists");
    }

    sparkPath.addPointToSegment(x, y);
  }

  void reset() {
    for (SparkPathType pathType : paths.keySet()) {
      paths.get(pathType).reset();
    }
  }


}
