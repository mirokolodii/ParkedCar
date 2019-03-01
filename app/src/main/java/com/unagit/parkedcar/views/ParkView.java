package com.unagit.parkedcar.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.unagit.parkedcar.R;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class ParkView extends LinearLayout {

    private static int CONTAINER_WIDTH;
    private static int CIRCLE_WIDTH;
    private static int DIST_BETWEEN_CIRCLES;
    private static int CIRCLES_COUNT = 3;
    private static int[] CIRCLES_POS = new int[CIRCLES_COUNT];

    public ParkView(Context context) {
        super(context);
    }

    public ParkView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ParkView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView() {
        inflate(getContext(), R.layout.park_view, this);
        FrameLayout container = findViewById(R.id.circles_container);
        ArrayList<CircleView> circles = getCircles(CIRCLES_COUNT);
        for (CircleView circle : circles) {
            container.addView(circle);
        }
        Button btn = findViewById(R.id.park_car);
        btn.setOnClickListener(v -> startAnimation(circles));
//        Log.e("anim", "Number of circles: " + circles.size());

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
        setDimens();
    }

    private void setDimens() {
        FrameLayout container = findViewById(R.id.circles_container);
        CONTAINER_WIDTH = container.getWidth();
//        Log.e("anim", "Container width: " + container.getWidth());
        CIRCLE_WIDTH = container.getHeight();
        DIST_BETWEEN_CIRCLES = 10;
        CIRCLES_POS[0] = (int) (CONTAINER_WIDTH / 2.0 + CIRCLE_WIDTH / 2.0 + DIST_BETWEEN_CIRCLES);
        CIRCLES_POS[1] = (int) (CONTAINER_WIDTH / 2.0 - CIRCLE_WIDTH / 2.0);
        CIRCLES_POS[2] = (int) (CONTAINER_WIDTH / 2.0 - 3 * CIRCLE_WIDTH / 2.0 - DIST_BETWEEN_CIRCLES);
    }

    // https://www.raywenderlich.com/350-android-animation-tutorial-with-kotlin
    private void startAnimation(ArrayList<CircleView> circles) {
        AnimatorSet set = new AnimatorSet();
        Animator enterAnimForCircleOne
                = getAnimSet(circles.get(0), 0, CIRCLES_POS[0], 0f, 1f);
        Animator enterAnimForCircleTwo
                = getAnimSet(circles.get(1), 0, CIRCLES_POS[1], 0f, 1f);
        Animator enterAnimForCircleThree
                = getAnimSet(circles.get(2), 0, CIRCLES_POS[2], 0f, 1f);

        Animator exitAnimForCircleOne
                = getAnimSet(circles.get(0), CIRCLES_POS[0], CONTAINER_WIDTH, 1f, 0f);
        Animator exitAnimForCircleTwo
                = getAnimSet(circles.get(1), CIRCLES_POS[1], CONTAINER_WIDTH,1f, 0f);
        Animator exitAnimForCircleThree
                = getAnimSet(circles.get(2), CIRCLES_POS[2], CONTAINER_WIDTH,1f, 0f);

//        set.play(animForCircleOne).before(animForCircleTwo);
//        set.play(animForCircleTwo).before(animForCircleThree);
        set.playSequentially(
                enterAnimForCircleOne,
                enterAnimForCircleTwo,
                enterAnimForCircleThree,
                exitAnimForCircleOne,
                exitAnimForCircleTwo,
                exitAnimForCircleThree);

//        Log.e("anim", "CONTAINER WIDTH: " + CONTAINER_WIDTH);
        set.setDuration(500);

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.e("anim", "ended");
                set.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        set.start();
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

    private ObjectAnimator getFadeAnimator(View view, float startFadeValue, float endFadeValue ) {
        return ObjectAnimator.ofFloat(view, "alpha", startFadeValue, endFadeValue);
    }
}
