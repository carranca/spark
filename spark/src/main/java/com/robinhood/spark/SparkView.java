/**
 * Copyright (C) 2016 Robinhood Markets, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.robinhood.spark;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import com.robinhood.spark.animation.SparkAnimator;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.robinhood.spark.GraphInteractionState.DEFAULT;
import static com.robinhood.spark.GraphInteractionState.SCRUBBED;
import static com.robinhood.spark.GraphInteractionState.UNSCRUBBED;

/**
 * A {@link SparkView} is a simplified line chart with no axes.
 */
public class SparkView extends View implements ScrubGestureDetector.ScrubListener {
    private @Nullable Float scrubLine;

    /**
     * Holds the fill type constants to be used with {@linkplain #getFillType()} and
     * {@linkplain #setFillType(int)}
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            FillType.NONE,
            FillType.UP,
            FillType.DOWN,
            FillType.TOWARD_ZERO,
    })
    public @interface FillType {
        /**
         * Fill type constant for having no fill on the graph
         */
        int NONE = 0;

        /**
         * Fill type constant for always filling the area above the sparkline.
         */
        int UP = 1;

        /**
         * Fill type constant for always filling the area below the sparkline
         */
        int DOWN = 2;

        /**
         * Fill type constant for filling toward zero. This will fill downward if your sparkline is
         * positive, or upward if your sparkline is negative. If your sparkline intersects zero,
         * each segment will still color toward zero.
         */
        int TOWARD_ZERO = 3;
    }

    private float legacyLineWidth;
    @FillType private int fillType = FillType.NONE;
    private float eventDotRadius;
    private boolean scrubEnabled;
    private @Nullable SparkAnimator sparkAnimator;

    // the onDraw data
    private SparkPaths sparkPaths = new SparkPaths();
    private SparkPaths renderPaths = new SparkPaths();
    private final Path baseLinePath = new Path();
    private final Path scrubLinePath = new Path();
    private final Path eventsPath = new Path();

    // adapter
    private @Nullable SparkAdapter adapter;

    // misc fields
    private ScaleHelper scaleHelper;

    private Map<SparkPathType, Paint> defaultLinePaints = new HashMap<>();
    private Map<SparkPathType, Paint> scrubbedLinePaints = new HashMap<>();
    private Map<SparkPathType, Paint> unscrubbedLinePaints = new HashMap<>();
    private Map<SparkPathType, Paint> defaultFillPaints = new HashMap<>();
    private Map<SparkPathType, Paint> scrubbedFillPaints = new HashMap<>();
    private Map<SparkPathType, Paint> unscrubbedFillPaints = new HashMap<>();
    private Map<SparkPathType, Paint> defaultEventPaints = new HashMap<>();
    private Map<SparkPathType, Paint> scrubbedEventPaints = new HashMap<>();
    private Map<SparkPathType, Paint> unscrubbedEventPaints = new HashMap<>();

    private Paint baseLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint scrubLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private @Nullable OnScrubListener scrubListener;
    private @NonNull ScrubGestureDetector scrubGestureDetector;
    private @Nullable Animator pathAnimator;
    private final RectF contentRect = new RectF();
    private @Nullable RectF contentClip = null;

    private List<Float> xPoints;

    public SparkView(Context context) {
        super(context);
        init(context, null, R.attr.spark_SparkViewStyle, R.style.spark_SparkView);
    }

    public SparkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.spark_SparkViewStyle, R.style.spark_SparkView);
    }

    public SparkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, R.style.spark_SparkView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SparkView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SparkView,
                defStyleAttr, defStyleRes);

        legacyLineWidth = a.getDimension(R.styleable.SparkView_spark_lineWidth, 0);

        scrubEnabled = a.getBoolean(R.styleable.SparkView_spark_scrubEnabled, true);
        eventDotRadius = a.getDimension(R.styleable.SparkView_spark_eventDotRadius, 2.0f);
        a.recycle();

        final Handler handler = new Handler();
        final float touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        scrubGestureDetector = new ScrubGestureDetector(this, handler, touchSlop);
        scrubGestureDetector.setEnabled(scrubEnabled);
        setOnTouchListener(scrubGestureDetector);

        xPoints = new ArrayList<>();

        if (isInEditMode()) {
            this.setAdapter(new SparkAdapter() {
                private final float[] yData = new float[] {68,22,31,57,35,79,86,47,34,55,80,72,99,66,47,42,56,64,66,80,97,10,43,12,25,71,47,73,49,36};
                @Override public int getCount() { return yData.length; }
                @NonNull @Override public Object getItem(int index) { return yData[index]; }
                @Override public float getY(int index) { return yData[index]; }

                @Override protected SparkPathType getPathType(int index) {
                    return SparkPathType.Legacy.INSTANCE;
                }

                @Override protected Set<SparkPathType> getSupportedPathTypes() {
                    Set<SparkPathType> types = new HashSet<>();
                    types.add(SparkPathType.Legacy.INSTANCE);
                    return types;
                }
            });
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        updateContentRect();
        populatePath();
    }

    /**
     * Populates the {@linkplain #sparkPaths} with points
     */
    private void populatePath() {
        if (adapter == null) return;
        if (getWidth() == 0 || getHeight() == 0) return;

        final int adapterCount = adapter.getCount();

        // to draw anything, we need 2 or more points
        if (adapterCount < 2) {
            clearData();
            return;
        }

        scaleHelper = new ScaleHelper(adapter, contentRect, legacyLineWidth, fillType != FillType.NONE);

        // Reset points caches
        xPoints.clear();

        // make our main graph path
        eventsPath.reset();

        // Reset all of our paths.
        sparkPaths.reset();

        SparkPathType currentPathType = null;

        for (int i = 0; i < adapterCount; i++) {
            final float x = scaleHelper.getX(adapter.getX(i));
            final float y = scaleHelper.getY(adapter.getY(i));

            xPoints.add(x);

            final SparkPathType pathType = adapter.getPathType(i);

            if (currentPathType == null) {
                sparkPaths.startPathSegment(pathType, x, y);
                currentPathType = pathType;
            }

            if (!pathType.equals(currentPathType)) {
                // We're starting a new path, so the current one ends here.
                sparkPaths.endPathSegment(currentPathType, getFillEdge(), getPaddingStart());

                // Start a new path.
                sparkPaths.startPathSegment(pathType, x, y);
                currentPathType = pathType;
            } else {
                sparkPaths.addToPathSegment(pathType, x, y);
            }

            // If this is a special event, it needs some extra processing.
            if (adapter.isEvent(i)) {
                Path dot = new Path();
                dot.moveTo(x, y);
                dot.addCircle(x, y, eventDotRadius, Path.Direction.CW);
                dot.close();
                eventsPath.addPath(dot);
            }
        }

        // Add the last path to the list of paths.
        sparkPaths.endPathSegment(currentPathType, getFillEdge(), getPaddingStart());

        // make our base line path
        baseLinePath.reset();
        if (adapter.hasBaseLine()) {
            float scaledBaseLine = scaleHelper.getY(adapter.getBaseLine());
            baseLinePath.moveTo(0, scaledBaseLine);
            baseLinePath.lineTo(getWidth(), scaledBaseLine);
        }

        renderPaths.reset();
        renderPaths = new SparkPaths(sparkPaths);

        contentClip = null;

        if (sparkAnimator != null) {
            sparkAnimator.onNewPathsPopulated(this);
        }

        invalidate();
    }

    @Nullable
    private Float getFillEdge() {
        switch (fillType) {
            case FillType.NONE:
                return null;
            case FillType.UP:
                return (float) getPaddingTop();
            case FillType.DOWN:
                return (float) getHeight() - getPaddingBottom();
            case FillType.TOWARD_ZERO:
                float zero = scaleHelper.getY(0F);
                float bottom = (float) getHeight() - getPaddingBottom();
                return Math.min(zero, bottom);
            default:
                throw new IllegalStateException(
                        String.format(Locale.US, "Unknown fill-type: %d", fillType)
                );
        }
    }

    public SparkPaths getSparkPaths() {
        return new SparkPaths(sparkPaths);
    }

    public RectF getContentRect() {
        return contentRect;
    }

    /**
     * Set the path to animate in onDraw, used for getAnimation purposes
     */
    public void setAnimationPath(SparkPaths animationPath) {
        renderPaths.reset();
        renderPaths = new SparkPaths(animationPath);
        invalidate();
    }

    public void setContentClip(@Nullable RectF newContentClip) {
        this.contentClip = newContentClip;
        invalidate();
    }

    private void setScrubLine(@Nullable Float scrubLineX) {
        scrubLinePath.reset();

        if (scrubLineX == null) {
            scrubLine = null;
        } else {
            scrubLine = resolveBoundedScrubLine(scrubLineX);
            scrubLinePath.moveTo(scrubLine, getPaddingTop());
            scrubLinePath.lineTo(scrubLine, getHeight() - getPaddingBottom());
        }

        invalidate();
    }

    /**
     * Bounds the x coordinate of a scrub within the bounding rect minus padding and line width.
     */
    private float resolveBoundedScrubLine(float x) {
        float scrubLineOffset = scrubLinePaint.getStrokeWidth() / 2;

        float leftBound = getPaddingStart() + scrubLineOffset;
        if (x < leftBound) {
            return leftBound;
        }

        float rightBound = getWidth() - getPaddingEnd() - scrubLineOffset;
        if (x > rightBound) {
            return rightBound;
        }

        return x;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        updateContentRect();
        populatePath();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.drawPath(baseLinePath, baseLinePaint);
        canvas.restore();

        RectF clip = contentClip;
        if (clip == null) {
            // No clipping, in practice.
            clip = contentRect;
        }

        canvas.drawPath(scrubLinePath, scrubLinePaint);

        canvas.clipRect(clip);

        for (SparkPathType pathType : renderPaths.paths.keySet()) {
            SparkPath sparkPath = renderPaths.paths.get(pathType);
            for (SparkPath.SparkPathSegment segment : sparkPath.segments) {

                if (scrubLine != null) {
                    // Draw and clip the scrubbed path
                    canvas.save();
                    canvas.clipRect(
                        contentRect.left,
                        contentRect.top,
                        scrubLine - 1,
                        contentRect.bottom);
                    canvas.drawPath(segment, scrubbedLinePaints.get(pathType));
                    if (fillType != FillType.NONE) {
                        canvas.drawPath(segment, scrubbedFillPaints.get(pathType));
                    }

                    // Draw events in the same clipping area.
                    canvas.drawPath(eventsPath, scrubbedEventPaints.get(pathType));

                    canvas.restore();

                    // Draw and clip the unscrubbed path
                    canvas.save();
                    canvas.clipRect(
                        scrubLine + 1,
                        contentRect.top,
                        contentRect.right,
                        contentRect.bottom);
                    canvas.drawPath(segment, unscrubbedLinePaints.get(pathType));
                    if (fillType != FillType.NONE) {
                        canvas.drawPath(segment, unscrubbedFillPaints.get(pathType));
                    }

                    // Draw events in the same clipping area.
                    canvas.drawPath(eventsPath, unscrubbedEventPaints.get(pathType));

                    canvas.restore();
                } else {
                    canvas.drawPath(segment, defaultLinePaints.get(pathType));
                    if (fillType != FillType.NONE) {
                        canvas.drawPath(segment, defaultFillPaints.get(pathType));
                    }

                    canvas.drawPath(eventsPath, defaultEventPaints.get(pathType));
                }
            }
        }
    }

    public void updateStyling() {

        if (adapter == null) {
            // Nothing to do here.
            return;
        }

        // Clear existing paints
        unscrubbedLinePaints.clear();
        defaultLinePaints.clear();
        scrubbedLinePaints.clear();
        defaultEventPaints.clear();
        scrubbedEventPaints.clear();
        unscrubbedEventPaints.clear();
        defaultFillPaints.clear();
        scrubbedFillPaints.clear();
        unscrubbedFillPaints.clear();

        final SparkPaintProvider paintProvider = adapter.getPaintProvider();

        for (SparkPathType pathType : adapter.getSupportedPathTypes()) {
            defaultLinePaints.put(pathType, paintProvider.getPathPaint(getContext(), pathType, DEFAULT));
            scrubbedLinePaints.put(pathType, paintProvider.getPathPaint(getContext(), pathType, SCRUBBED));
            unscrubbedLinePaints.put(pathType, paintProvider.getPathPaint(getContext(), pathType, UNSCRUBBED));
            defaultEventPaints.put(pathType, paintProvider.getEventPaint(getContext(), pathType, DEFAULT));
            scrubbedEventPaints.put(pathType, paintProvider.getEventPaint(getContext(), pathType, SCRUBBED));
            unscrubbedEventPaints.put(pathType, paintProvider.getEventPaint(getContext(), pathType, UNSCRUBBED));
            defaultFillPaints.put(pathType, paintProvider.getPathFillPaint(getContext(), pathType, DEFAULT));
            scrubbedFillPaints.put(pathType, paintProvider.getPathFillPaint(getContext(), pathType, SCRUBBED));
            unscrubbedFillPaints.put(pathType, paintProvider.getPathFillPaint(getContext(), pathType, UNSCRUBBED));
        }

        scrubLinePaint = paintProvider.getScrubLinePaint(getContext());
        baseLinePaint = paintProvider.getBaselinePaint(getContext());

        invalidate();
    }

    /**
     * Animator class to animate Spark
     * @return a {@link SparkAnimator} or null
     */
    @Nullable
    public SparkAnimator getSparkAnimator() {
        return sparkAnimator;
    }

    /**
     * Animator class to animate Spark
     * @param sparkAnimator - a {@link SparkAnimator}
     */
    public void setSparkAnimator(@Nullable SparkAnimator sparkAnimator) {
        this.sparkAnimator = sparkAnimator;
    }

    @FillType
    public int getFillType() {
        return fillType;
    }

    public void setFillType(@FillType int fillType) {
        if (this.fillType != fillType) {
            this.fillType = fillType;
            populatePath();
        }
    }

    /**
     * Get the radius for the dot rendered when a graph event occurs.
     */
    public float getEventDotRadius() {
        return eventDotRadius;
    }

    /**
     * Set the radius for the dot rendered when a graph event occurs.
     */
    public void setEventDotRadius(float eventDotRadius) {
        this.eventDotRadius = eventDotRadius;
        invalidate();
    }

    /**
     * Return true if scrubbing is enabled on this view
     */
    public boolean isScrubEnabled() {
        return scrubEnabled;
    }

    /**
     * Set whether or not to enable scrubbing on this view.
     */
    public void setScrubEnabled(boolean scrubbingEnabled) {
        this.scrubEnabled = scrubbingEnabled;
        scrubGestureDetector.setEnabled(scrubbingEnabled);
        invalidate();
    }

    /**
     * Get the current {@link OnScrubListener}
     */
    @Nullable
    public OnScrubListener getScrubListener() {
        return scrubListener;
    }

    /**
     * Set a {@link OnScrubListener} to be notified of the user's scrubbing gestures.
     */
    public void setScrubListener(@Nullable OnScrubListener scrubListener) {
        this.scrubListener = scrubListener;
    }

    /**
     * Get the backing {@link SparkAdapter}
     */
    @Nullable
    public SparkAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the backing {@link SparkAdapter} to generate the points to be graphed
     */
    public void setAdapter(@Nullable SparkAdapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(dataSetObserver);
        }

        this.adapter = adapter;

        if (this.adapter != null) {
            this.adapter.registerDataSetObserver(dataSetObserver);
        }

        updateStyling();

        populatePath();
    }

    /**
     * Returns a copy of current graphic X points
     * @return current graphic X points
     */
    @NonNull
    public List<Float> getXPoints() {
        return new ArrayList<>(xPoints);
    }

    private void doPathAnimation() {
        if (pathAnimator != null) {
            pathAnimator.cancel();
        }

        pathAnimator = getAnimator();

        if (pathAnimator != null) {
            pathAnimator.start();
        }
    }

    @Nullable
    private Animator getAnimator() {
        if (sparkAnimator != null) {
            // Okay, so we need to store every point in each path.
            // Then, the new, multipath-aware sparkAnimator will know to create an animation for
            // each path.
            return sparkAnimator.getAnimation(this);
        }

        return null;
    }

    private void clearData() {
        scaleHelper = null;
        sparkPaths.reset();
        renderPaths.reset();
        baseLinePath.reset();
        eventsPath.reset();
        invalidate();
    }

    /**
     * Helper class for handling scaling logic.
     */
    static class ScaleHelper {
        // the width and height of the view
        final float width, height;
        final int size;
        // the scale factor for the Y values
        final float xScale, yScale;
        // translates the Y values back into the bounding rect after being scaled
        final float xTranslation, yTranslation;

        ScaleHelper(SparkAdapter adapter, RectF contentRect, float lineWidth, boolean fill) {
            final float leftPadding = contentRect.left;
            final float topPadding = contentRect.top;

            // subtract legacyLineWidth to offset for 1/2 of the line bleeding out of the content box on
            // either side of the view
            final float lineWidthOffset = fill ? 0 : lineWidth;
            this.width = contentRect.width() - lineWidthOffset;
            this.height = contentRect.height() - lineWidthOffset;

            this.size = adapter.getCount();

            // get data bounds from adapter
            RectF bounds = adapter.getDataBounds();

            // if data is a line (which technically has no size), expand bounds to center the data
            bounds.inset(bounds.width() == 0 ? -1 : 0, bounds.height() == 0 ? -1 : 0);

            final float minX = bounds.left;
            final float maxX = bounds.right;
            final float minY = bounds.top;
            final float maxY = bounds.bottom;

            // xScale will compress or expand the min and max x values to be just inside the view
            this.xScale = width / (maxX - minX);
            // xTranslation will move the x points back between 0 - width
            this.xTranslation = leftPadding - (minX * xScale) + (lineWidthOffset / 2);
            // yScale will compress or expand the min and max y values to be just inside the view
            this.yScale = height / (maxY - minY);
            // yTranslation will move the y points back between 0 - height
            this.yTranslation = minY * yScale + topPadding + (lineWidthOffset / 2);
        }

        /**
         * Given the 'raw' X value, scale it to fit within our view.
         */
        public float getX(float rawX) {
            return rawX * xScale + xTranslation;
        }

        /**
         * Given the 'raw' Y value, scale it to fit within our view. This method also 'flips' the
         * value to be ready for drawing.
         */
        public float getY(float rawY) {
            return height - (rawY * yScale) + yTranslation;
        }
    }

    @Override
    public int getPaddingStart() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1
                ? super.getPaddingStart()
                : getPaddingLeft();
    }

    @Override
    public int getPaddingEnd() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1
                ? super.getPaddingEnd()
                : getPaddingRight();
    }

    /**
     * Gets the rect representing the 'content area' of the view. This is essentially the bounding
     * rect minus any padding.
     */
    private void updateContentRect() {
        contentRect.set(
                getPaddingStart(),
                getPaddingTop(),
                getWidth() - getPaddingEnd(),
                getHeight() - getPaddingBottom()
        );
    }

    /**
     * returns the nearest index (into {@link #adapter}'s data) for the given x coordinate.
     */
    static int getNearestIndex(List<Float> points, float x) {
        int index = Collections.binarySearch(points, x);

        // if binary search returns positive, we had an exact match, return that index
        if (index >= 0) return index;

        // otherwise, calculate the binary search's specified insertion index
        index = - 1 - index;

        // if we're inserting at 0, then our guaranteed nearest index is 0
        if (index == 0) return index;

        // if we're inserting at the very end, then our guaranteed nearest index is the final one
        if (index == points.size()) return --index;

        // otherwise we need to check which of our two neighbors we're closer to
        final float deltaUp = points.get(index) - x;
        final float deltaDown = x - points.get(index - 1);
        if (deltaUp > deltaDown) {
            // if the below neighbor is closer, decrement our index
            index--;
        }

        return index;
    }

    @Override
    public void onScrubbed(float x, float y) {
        if (adapter == null || adapter.getCount() == 0) return;
        if (scrubListener != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
            int index = getNearestIndex(xPoints, x);
            if (scrubListener != null) {
                scrubListener.onScrubbed(adapter.getItem(index));
            }
        }

        setScrubLine(x);
    }

    @Override
    public void onScrubEnded() {
        if (scrubListener != null) scrubListener.onScrubbed(null);
        setScrubLine(null);
    }

    /**
     * Listener for a user scrubbing (dragging their finger along) the graph.
     */
    public interface OnScrubListener {
        /**
         * Indicates the user is currently scrubbing over the given value. A null value indicates
         * that the user has stopped scrubbing.
         */
        void onScrubbed(@Nullable Object value);
    }

    private final DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            populatePath();

            if (sparkAnimator != null) {
                doPathAnimation();
            }
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            clearData();
        }
    };
}
