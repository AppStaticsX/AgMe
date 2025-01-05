package com.appstaticsx.app.agmeapp;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.List;

public class TapTargetSequenceHelper {

    public static void showTapTargetSequence(
            Activity activity,
            List<TapTarget> tapTargets,
            TapTargetSequence.Listener listener) {

        if (tapTargets == null || tapTargets.isEmpty()) {
            throw new IllegalArgumentException("TapTargets list cannot be null or empty.");
        }

        TapTargetSequence sequence = new TapTargetSequence(activity)
                .targets(tapTargets)
                .listener(listener != null ? listener : new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        Toast.makeText(activity, "Sequence Finished", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        Toast.makeText(activity, "GREAT!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        Toast.makeText(activity, "Sequence Canceled", Toast.LENGTH_SHORT).show();
                    }
                });

        sequence.start();
    }

    public static TapTarget createTapTarget(
            View view,
            String title,
            String description,
            int outerCircleColor,
            int targetCircleColor,
            int titleTextColor,
            int descriptionTextColor,
            Typeface typeface,
            int targetRadius) {

        return TapTarget.forView(view, title, description)
                .outerCircleColor(outerCircleColor)
                .outerCircleAlpha(0.96f)
                .targetCircleColor(targetCircleColor)
                .titleTextSize(20)
                .titleTextColor(titleTextColor)
                .descriptionTextSize(10)
                .descriptionTextColor(descriptionTextColor)
                .textTypeface(typeface)
                .dimColor(android.R.color.black)
                .drawShadow(true)
                .cancelable(false)
                .tintTarget(true)
                .transparentTarget(true)
                .targetRadius(targetRadius);
    }
}
