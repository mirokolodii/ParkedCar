package com.unagit.parkedcar.views.park;

import android.content.Context;
import android.content.res.Configuration;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import androidx.appcompat.widget.AppCompatButton;
import com.unagit.parkedcar.R;
import com.unagit.parkedcar.helpers.Constants;

public class ParkButton extends AppCompatButton {
    public ParkButton(Context context) {
        super(context);
    }

    public ParkButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ParkButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setParked(Boolean isParked) {
        beginTransitionWithAnim(isParked);
    }

    void setWorking() {
        setEnabled(false);
        setText(getContext().getString(R.string.park_btn_working));
    }

    private void beginTransitionWithAnim(final Boolean isParked) {
        // Declare transition for button
        ChangeBounds buttonTransition = new ChangeBounds();
        buttonTransition
                .setInterpolator(new AnticipateInterpolator())
                .setDuration(500)
                .addTarget(this);
        buttonTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                // Remove text, so it's not "jumping" during animation
                setText("");
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                // Show button text again on transition end.
                setText(isParked
                        ? getContext().getString(R.string.park_btn_clear)
                        : getContext().getString(R.string.park_btn_park_car));
                setEnabled(true);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        // Initialize DelayedTransition
        ViewGroup container = (ViewGroup) getParent();
        TransitionManager.beginDelayedTransition(container, buttonTransition);

        // Begin transition
        changePadding(isParked, getResources().getConfiguration().orientation);
    }

    private void changePadding(Boolean isParked, int orientation) {
        int[] paddingValues;
        switch (orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                paddingValues = isParked
                        ? Constants.ParkButtonPadding.SMALL_PORTRAIT
                        : Constants.ParkButtonPadding.BIG_PORTRAIT;
                break;

            case Configuration.ORIENTATION_LANDSCAPE:
                paddingValues = isParked
                        ? Constants.ParkButtonPadding.SMALL_LANDSCAPE
                        : Constants.ParkButtonPadding.BIG_LANDSCAPE;
                break;

            default:
                paddingValues = Constants.ParkButtonPadding.BIG_PORTRAIT;
        }

        setPadding(
                DPToPixels(paddingValues[0]),
                DPToPixels(paddingValues[1]),
                DPToPixels(paddingValues[2]),
                DPToPixels(paddingValues[3])
        );

    }


    /**
     * Converts DP units to pixels.
     *
     * @param sizeInDp as integer.
     * @return pixels as integer.
     */
    private int DPToPixels(int sizeInDp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (sizeInDp * scale + 0.5f);
    }
}
