package com.robinhood.spark;

import android.graphics.Paint;
import android.support.annotation.Nullable;
import java.util.Map;

public class SparkViewModel {

  final Map<SparkPathType, Map<GraphInteractionState, PaintTransformer>> graphLineConfiguration;

  public SparkViewModel(
      Map<SparkPathType, Map<GraphInteractionState, PaintTransformer>> graphLineConfiguration
  ) {
    this.graphLineConfiguration = graphLineConfiguration;
  }

  public interface SparkPathType {}
  public enum LegacySparkPathType implements SparkPathType {
    INSTANCE
  }

  public enum GraphInteractionState {
    DEFAULT,
    SCRUBBED,
    UNSCRUBBED
  }

  public interface PaintTransformer {
    void apply(Paint paint);
  }
}
