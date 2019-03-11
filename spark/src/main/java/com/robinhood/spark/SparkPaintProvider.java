package com.robinhood.spark;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

/**
 * Class responsible for providing paints that the {@link SparkView} can use to draw the various
 * components and supported path types of the graph.
 * Inheritors of this class are strongly encouraged to call into the superclass for any defaults
 * and sane starting points for customization; otherwise they MUST ensure they provide
 * Paint instances for every path type reported in {@link SparkAdapter#getSupportedPathTypes()}
 */
public class SparkPaintProvider {
  private final Paint defaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint defaultEventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint defaultScrubLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint defaultBaselinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint defaultFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  protected SparkPaintProvider() {
    defaultPaint.setStyle(Paint.Style.STROKE);
    defaultPaint.setStrokeCap(Paint.Cap.ROUND);

    defaultEventPaint.set(defaultPaint);
    defaultEventPaint.setStyle(Paint.Style.FILL);
    defaultEventPaint.setStrokeWidth(0);

    defaultScrubLinePaint.setStyle(Paint.Style.STROKE);
    defaultScrubLinePaint.setStrokeCap(Paint.Cap.ROUND);

    defaultBaselinePaint.setStyle(Paint.Style.STROKE);

    defaultFillPaint.set(defaultPaint);
    defaultFillPaint.setStyle(Paint.Style.FILL);
    defaultFillPaint.setStrokeWidth(0);
  }

  /**
   * Returns the exact Paint the {@link SparkView} should use to draw lines in the graph of the
   * specified {@link SparkPathType} that are in the specified {@link GraphInteractionState} state.
   * Overriding implementations are encouraged to call into the {@link SparkPaintProvider}
   * implementation for defaults and sane starting points for customization.
   */
  @CallSuper
  protected Paint getPathPaint(
      @NonNull Context context,
      @NonNull SparkPathType pathType,
      @NonNull GraphInteractionState state
  ) {
    return defaultPaint;
  }

  /**
   * Returns the exact Paint the {@link SparkView} should use to draw the graph's scrubline.
   * Overriding implementations are encouraged to call into the {@link SparkPaintProvider}
   * implementation for defaults and sane starting points for customization.
   */
  @CallSuper
  protected Paint getScrubLinePaint(@NonNull Context context) {
    return defaultScrubLinePaint;
  }

  /**
   * Returns the exact Paint the {@link SparkView} should use to draw the graph's baseline.
   * Overriding implementations are encouraged to call into the {@link SparkPaintProvider}
   * implementation for defaults and sane starting points for customization.
   */
  @CallSuper
  protected Paint getBaselinePaint(@NonNull Context context) {
    return defaultBaselinePaint;
  }

  /**
   * Returns the exact Paint the {@link SparkView} should use to draw events in the graph of the
   * specified {@link SparkPathType} that are in the specified {@link GraphInteractionState} state.
   * Overriding implementations are encouraged to call into the {@link SparkPaintProvider}
   * implementation for defaults and sane starting points for customization.
   */
  @CallSuper
  protected Paint getEventPaint(
      @NonNull Context context,
      @NonNull SparkPathType pathType,
      @NonNull GraphInteractionState state
  ) {
    return defaultEventPaint;
  }

  /**
   * Returns the exact Paint the {@link SparkView} should use to fill sections of the graph of the
   * specified {@link SparkPathType} that are in the specified {@link GraphInteractionState} state.
   * Overriding implementations are encouraged to call into the {@link SparkPaintProvider}
   * implementation for defaults and sane starting points for customization.
   */
  @CallSuper
  protected Paint getPathFillPaint(
      @NonNull Context context,
      @NonNull SparkPathType pathType,
      @NonNull GraphInteractionState state
  ) {
    return defaultFillPaint;
  }
}
