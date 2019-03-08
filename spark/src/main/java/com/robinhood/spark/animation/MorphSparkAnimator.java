package com.robinhood.spark.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.util.Log;
import com.robinhood.spark.SparkPath;
import com.robinhood.spark.SparkPaths;
import com.robinhood.spark.SparkView;

import com.robinhood.spark.SparkViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Animates each point vertically from the previous position to the current position.
 */
public class MorphSparkAnimator extends Animator implements SparkAnimator {

    private final ValueAnimator animator;
    private Map<SparkPath.SparkPathSegment, List<Float>> oldYPointsBySegment = new HashMap<>();
    private boolean animate;

    public MorphSparkAnimator() {
        animator = ValueAnimator.ofFloat(0, 1);
    }

    @Nullable
    @Override
    public Animator getAnimation(final SparkView sparkView) {

        final SparkPaths sparkPaths = sparkView.getSparkPaths();

        final Map<SparkPath.SparkPathSegment, List<Float>> xPointsBySegment = new HashMap<>();
        final Map<SparkPath.SparkPathSegment, List<Float>> yPointsBySegment = new HashMap<>();

        for (SparkPath sparkPath : sparkPaths.paths.values()) {
            for (SparkPath.SparkPathSegment segment : sparkPath.segments) {
                if (!segment.xPoints.isEmpty()) {
                    xPointsBySegment.put(segment, new ArrayList<>(segment.xPoints));
                }

                if (!segment.yPoints.isEmpty()) {
                    yPointsBySegment.put(segment, new ArrayList<>(segment.yPoints));
                }

            }
        }

        if (!animate) {
            oldYPointsBySegment = yPointsBySegment;
            return null;
        }

        if (xPointsBySegment.isEmpty() || yPointsBySegment.isEmpty()) {
            return null;
        }

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float animatedValue = (float) animation.getAnimatedValue();
                for (SparkPath.SparkPathSegment segment : xPointsBySegment.keySet()) {
                    @Nullable List<Float> oldYPoints = oldYPointsBySegment.get(segment);
                    List<Float> xPoints = xPointsBySegment.get(segment);
                    List<Float> yPoints = yPointsBySegment.get(segment);

                    float step;
                    float x, y, oldY;

                    segment.reset();

                    for (int i = 0; i < xPoints.size(); i++) {
                        // get oldY, can be 0 (zero) if current points are larger
                        oldY = oldYPoints != null && oldYPoints.size() > i ? oldYPoints.get(i) : 0f;

                        step = yPoints.get(i) - oldY;
                        y = (step * animatedValue) + oldY;
                        x = xPoints.get(i);

                        if (i == 0) {
                            segment.moveTo(x, y);
                        } else {
                            segment.lineTo(x, y);
                        }
                    }

                }

                sparkView.setAnimationPath(sparkPaths);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                oldYPointsBySegment = yPointsBySegment;
            }
        });

        return animator;
    }

    @Override public void onNewPathsPopulated(SparkView sparkView) {
        if (!oldYPointsBySegment.isEmpty()) {
            return;
        }

        for (SparkPath sparkPath : sparkView.getSparkPaths().paths.values()) {
            for (SparkPath.SparkPathSegment segment : sparkPath.segments) {
                if (!segment.yPoints.isEmpty()) {
                    oldYPointsBySegment.put(segment, new ArrayList<>(segment.yPoints));
                }

            }
        }
    }

    @Override
    public long getStartDelay() {
        return animator.getStartDelay();
    }

    @Override
    public void setStartDelay(@IntRange(from = 0) long startDelay) {
        animator.setStartDelay(startDelay);
    }

    @Override
    public Animator setDuration(@IntRange(from = 0) long duration) {
        return animator.setDuration(duration);
    }

    @Override
    public long getDuration() {
        return animator.getDuration();
    }

    @Override
    public void setInterpolator(@Nullable TimeInterpolator timeInterpolator) {
        animator.setInterpolator(timeInterpolator);
    }

    @Override
    public boolean isRunning() {
        return animator.isRunning();
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }
}
