package com.robinhood.spark.animation;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import com.robinhood.spark.SparkPath;
import com.robinhood.spark.SparkPaths;
import com.robinhood.spark.SparkView;
import com.robinhood.spark.SparkViewModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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

        //final SparkPaths sparkPaths = sparkView.getSparkPaths();
        //
        //// Pre-calculate all the path measures
        //final TreeMap<SparkPath.SparkPathSegment, PathMeasure> pathsWithMeasures = new TreeMap<>(
        //    new Comparator<SparkPath.SparkPathSegment>() {
        //        @Override
        //        public int compare(SparkPath.SparkPathSegment o1, SparkPath.SparkPathSegment o2) {
        //            if (o1.pathType == o2.pathType && o1.xPoints.equals(o2.xPoints)) {
        //                //Log.d("SparkView", "compare " + o1 + " & " + o2 + " = 0");
        //                return 0;
        //            }
        //
        //            if (o1.xPoints.isEmpty()) {
        //                //Log.d("SparkView", "compare " + o1 + " & " + o2 + " = -1 empty");
        //                return -1;
        //            }
        //
        //            if (o2.xPoints.isEmpty()) {
        //                //Log.d("SparkView", "compare " + o1 + " & " + o2 + " = 1 empty");
        //                return 1;
        //            }
        //
        //            int result = o1.xPoints.get(0) > o2.xPoints.get(0)
        //                ? 1
        //                : -1;
        //            //Log.d("SparkView", "compare " + o1 + " & " + o2 + " = " + result);
        //            return result;
        //        }
        //    });
        //
        //for (SparkPath fullPathOfType : sparkPaths.paths.values()) {
        //    for (SparkPath.SparkPathSegment segment : fullPathOfType.segments) {
        //        // get path length
        //        final PathMeasure pathMeasure = new PathMeasure(segment, false);
        //        final float endLength = pathMeasure.getLength();
        //
        //        if (endLength > 0) {
        //            pathsWithMeasures.put(segment, pathMeasure);
        //        }
        //    }
        //}
        //
        //if (pathsWithMeasures.isEmpty()) {
        //    return null;
        //}
        //
        //animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        //    @Override
        //    public void onAnimationUpdate(ValueAnimator animation) {
        //        float animatedValue = (float) animation.getAnimatedValue();
        //
        //        boolean flag = true;
        //        for (SparkPath.SparkPathSegment linePath : pathsWithMeasures.navigableKeySet()) {
        //            //if (flag) { flag = false; } else { continue; }
        //            //if (flag) { flag = false; continue; }
        //            PathMeasure pathMeasure = pathsWithMeasures.get(linePath);
        //            float endLength = pathMeasure.getLength();
        //            float animatedPathLength = animatedValue * endLength;
        //
        //            linePath.reset();
        //          boolean getSegmentResult = pathMeasure.getSegment(0, animatedPathLength, linePath, true);
        //          //sparkPaths.paths.get(linePath.pathType).replaceSegment(linePath.indexInSparkPath, linePath);
        //
        //          Log.d("LineSparkAnimator", "["+animatedValue+"]\tPath ("+linePath+")\t" + linePath.pathType.getClass().getSimpleName() + "["+linePath.indexInSparkPath+"]\t\tendLength="+endLength+"\tanimatedPathLength="+animatedPathLength + "\tgetSegmentResult=" + getSegmentResult);
        //        }
        //
        //        // set the updated path for the animation
        //        sparkView.setAnimationPath(sparkPaths);
        //    }
        //});









      //final Map<SparkPath, PathMeasure> pathsWithMeasures = new HashMap<>(sparkPaths.paths.size());
      //for (SparkPath linePath : sparkPaths.paths.values()) {
      //  // get path length
      //  final PathMeasure pathMeasure = new PathMeasure(linePath, false);
      //  final float endLength = pathMeasure.getLength();
      //
      //  if (endLength > 0) {
      //    pathsWithMeasures.put(linePath, pathMeasure);
      //  }
      //}
      //
      //if (pathsWithMeasures.isEmpty()) {
      //  return null;
      //}
      //
      //animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      //  @Override
      //  public void onAnimationUpdate(ValueAnimator animation) {
      //    float animatedValue = (float) animation.getAnimatedValue();
      //    Log.d("LineSparkAnimator", "AnimatedValue: " + animatedValue);
      //
      //    boolean flag = true;
      //    for (SparkPath linePath : pathsWithMeasures.keySet()) {
      //      //if (flag) { flag = false; } else { continue; }
      //      //if (flag) { flag = false; continue; }
      //      PathMeasure pathMeasure = pathsWithMeasures.get(linePath);
      //      float endLength = pathMeasure.getLength();
      //      float animatedPathLength = animatedValue * endLength;
      //
      //      linePath.reset();
      //      boolean getSegmentResult = pathMeasure.getSegment(0, animatedPathLength, linePath, true);
      //
      //      Log.d("LineSparkAnimator", "Path ("+linePath+")\t" + linePath.pathType + "\t\tendLength="+endLength+"\tanimatedPathLength="+animatedPathLength + "\tgetSegmentResult=" + getSegmentResult);
      //    }
      //
      //    // set the updated path for the animation
      //    sparkView.setAnimationPath(sparkPaths);
      //  }
      //});

        return animator;

        //final Path linePath = sparkView.getSparkLinePath();
        //
        //// get path length
        //final PathMeasure pathMeasure = new PathMeasure(linePath, false);
        //final float endLength = pathMeasure.getLength();
        //
        //if (endLength <= 0) {
        //    return null;
        //}
        //
        //animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        //    @Override
        //    public void onAnimationUpdate(ValueAnimator animation) {
        //        float animatedValue = (float) animation.getAnimatedValue();
        //
        //        float animatedPathLength = animatedValue * endLength;
        //
        //        linePath.reset();
        //        pathMeasure.getSegment(0, animatedPathLength, linePath, true);
        //
        //        // set the updated path for the animation
        //        sparkView.setAnimationPath(linePath);
        //    }
        //});
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
