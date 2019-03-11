package com.robinhood.spark.animation;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import com.robinhood.spark.SparkView;

/**
 * Animates the sparkline by path-tracing from the first point to the last.
 */
public class LineSparkAnimator extends Animator implements SparkAnimator {

    private final ValueAnimator animator;

    public LineSparkAnimator() {
        animator = ValueAnimator.ofFloat(0, 1);
    }

    @Nullable
    @Override
    public Animator getAnimation(final SparkView sparkView) {

        final RectF contentRect = sparkView.getContentRect();

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();

                float newWidth = (contentRect.right - contentRect.left) * animatedValue;
                sparkView.setContentClip(new RectF(
                    contentRect.left,
                    contentRect.top,
                    contentRect.left + newWidth,
                    contentRect.bottom));
            }
        });

        return animator;
    }

    @Override public void onNewPathsPopulated(SparkView sparkView) {
        // no-op
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
}
