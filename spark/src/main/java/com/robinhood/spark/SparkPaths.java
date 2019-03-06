package com.robinhood.spark;

import android.graphics.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class SparkPaths {
  final Map<SparkViewModel.SparkPathType, List<Path>> paths = new HashMap<>();

  void add(SparkPath path) {
    List<Path> pathsForType = paths.get(path.pathType);
    if (pathsForType == null) {
      pathsForType = new LinkedList<>();
    }

    pathsForType.add(path);
    paths.put(path.pathType, pathsForType);
  }

  void clear() {
    for (SparkViewModel.SparkPathType pathType : paths.keySet()) {
      for (Path path : paths.get(pathType)) {
        path.reset();
      }
    }

    paths.clear();
  }
}
