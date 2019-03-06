package com.unagit.parkedcar.views.park;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unagit.parkedcar.R;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class ParkViewImp extends LinearLayout implements ParkView {

    private static final int DIST_BETWEEN_CIRCLES = 10;
    private static final int CIRCLES_COUNT = 3;
    private AnimatorSet animatorSet = new AnimatorSet();
    private static final long ANIM_DURATION = 500;
    private ArrayList<CircleView> circles;
    private ParkButton parkButton;
    private boolean isRunningAnimation = false;
    private boolean isInitializedAnim = false;
    private boolean shouldStartAnimAfterFullInit = false;

    public ParkViewImp(Context context) {
        super(context);
    }

    public ParkViewImp(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ParkViewImp(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView() {
        inflate(getContext(), R.layout.park_view, this);
        FrameLayout container = findViewById(R.id.circles_container);
        circles = getCircles(CIRCLES_COUNT);
        for (CircleView circle : circles) {
            container.addView(circle);
        }
        parkButton = findViewById(R.id.park_car);
    }

    private ArrayList<CircleView> getCircles(Integer count) {
        ArrayList<CircleView> circles = new ArrayList<>();
        while (count > 0) {
            CircleView circleView = new CircleView(getContext());
            circleView.setAlpha(0);
            circles.add(circleView);
            count--;
        }
        return circles;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        initAnimation();
    }

     // https://www.raywenderlich.com/350-android-animation-tutorial-with-kotlin
    private void startAnimation() {
        if (!isInitializedAnim || isRunningAnimation) {
            shouldStartAnimAfterFullInit = true;
            return;
        }
        isRunningAnimation = true;
        animatorSet.start();
    }

    private void initAnimation() {
        if(isInitializedAnim) {
            return;
        }
        FrameLayout container = findViewById(R.id.circles_container);
        int CONTAINER_WIDTH = container.getWidth();
        int CIRCLE_DIAMETER = container.getHeight();
        int[] CIRCLES_POS = new int[CIRCLES_COUNT];
        CIRCLES_POS[0] = (int) (CONTAINER_WIDTH / 2.0 + CIRCLE_DIAMETER / 2.0 + DIST_BETWEEN_CIRCLES);
        CIRCLES_POS[1] = (int) (CONTAINER_WIDTH / 2.0 - CIRCLE_DIAMETER / 2.0);
        CIRCLES_POS[2] = (int) (CONTAINER_WIDTH / 2.0 - 3 * CIRCLE_DIAMETER / 2.0 - DIST_BETWEEN_CIRCLES);

        Animator enterAnimForCircleOne
                = getAnimSet(circles.get(0), 0, CIRCLES_POS[0], 0f, 1f);
        Animator enterAnimForCircleTwo
                = getAnimSet(circles.get(1), 0, CIRCLES_POS[1], 0f, 1f);
        Animator enterAnimForCircleThree
                = getAnimSet(circles.get(2), 0, CIRCLES_POS[2], 0f, 1f);

        Animator exitAnimForCircleOne
                = getAnimSet(circles.get(0), CIRCLES_POS[0], CONTAINER_WIDTH, 1f, 0f);
        Animator exitAnimForCircleTwo
                = getAnimSet(circles.get(1), CIRCLES_POS[1], CONTAINER_WIDTH, 1f, 0f);
        Animator exitAnimForCircleThree
                = getAnimSet(circles.get(2), CIRCLES_POS[2], CONTAINER_WIDTH, 1f, 0f);

        animatorSet.playSequentially(
                enterAnimForCircleOne,
                enterAnimForCircleTwo,
                enterAnimForCircleThree,
                exitAnimForCircleOne,
                exitAnimForCircleTwo,
                exitAnimForCircleThree);

        animatorSet.setDuration(ANIM_DURATION);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isRunningAnimation) {
                    animatorSet.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        isInitializedAnim = true;
        if(shouldStartAnimAfterFullInit) {
            startAnimation();
        }
    }

    private Animator getAnimSet(View view, int startPos, int endPos, float startFadeValue, float endFadeValue) {
        AnimatorSet set = new AnimatorSet();
        set.play(getPositionAnimator(view, startPos, endPos))
                .with(getFadeAnimator(view, startFadeValue, endFadeValue));
        return set;
    }

    private ObjectAnimator getPositionAnimator(View view, float startPos, float endPos) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", startPos, endPos);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    private ObjectAnimator getFadeAnimator(View view, float startFadeValue, float endFadeValue) {
        return ObjectAnimator.ofFloat(view, "alpha", startFadeValue, endFadeValue);
    }

    private void stopAnimation() {
        isRunningAnimation = false;
        for(View circle : circles) {
            circle.setAlpha(0);
            circle.setTranslationX(0);
        }
        animatorSet.end();
    }

    @Override
    public void setParkingTime(String time) {
        ((TextView) findViewById(R.id.park_time_info)).setText(time);
    }

    @Override
    public void setParking() {
        // 1. Enable Button
        parkButton.setParking();
        // 2. stop animation
        stopAnimation();
    }

    @Override
    public void clearParking() {
        // 1. Enable Button
        parkButton.clearParking();

        // 2. stop animation
        stopAnimation();
    }

    @Override
    public void setWaiting() {
        // 1. Disable Button
        parkButton.setWaiting();
        // 2. start animation
        startAnimation();
    }
}
