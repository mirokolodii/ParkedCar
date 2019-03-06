package com.unagit.parkedcar.views.park;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
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
    private boolean isParked = false;
    private boolean isInitialized = false;
    ChangeBounds buttonTransition = new ChangeBounds();

    public ParkButton(Context context) {
        super(context);
    }

    public ParkButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ParkButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInitialized) {
            setText(isParked
                    ? getContext().getString(R.string.park_btn_clear)
                    : getContext().getString(R.string.park_btn_park_car));
            isInitialized = true;
        initTransitionAnim();
        }
    }

    void setParking() {
        isParked = true;
        beginTransitionAnim(true);
//        setText(getContext().getString(R.string.park_btn_clear));
//        setEnabled(true);
    }

    void clearParking() {
        isParked = false;
        beginTransitionAnim(false);
//        setText(getContext().getString(R.string.park_btn_park_car));
//        setEnabled(true);
    }

    void setWaiting() {
        setEnabled(false);
        setText(getContext().getString(R.string.park_btn_working));
    }

    private void initTransitionAnim() {
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
    }

    private void beginTransitionAnim(final Boolean isParked) {
        ViewGroup container = (ViewGroup) getParent();
        TransitionManager.beginDelayedTransition(container, buttonTransition);
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
