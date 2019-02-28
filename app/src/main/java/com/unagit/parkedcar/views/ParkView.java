package com.unagit.parkedcar.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
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
    private static double[] CIRCLES_POS = new double[CIRCLES_COUNT];

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
        ArrayList<CircleView> circles = getCircles(1);//CIRCLES_COUNT);
        for(CircleView circle : circles) {
            container.addView(circle);
        }
        setDimens(container);
        Button btn = findViewById(R.id.park_car);
        btn.setOnClickListener(v -> startAnimation(circles));
        Log.e("anim", "Number of circles: " + circles.size());

    }

    private ArrayList<CircleView> getCircles(Integer count) {
        ArrayList<CircleView> circles = new ArrayList<>();
        while (count > 0) {
            circles.add(new CircleView(getContext()));
            count--;
        }
        return circles;
    }

    private void setDimens(FrameLayout container) {
        CONTAINER_WIDTH = container.getWidth();
        Log.e("anim", "Container width: " + container.getWidth());
        CIRCLE_WIDTH = container.getHeight();
        DIST_BETWEEN_CIRCLES = 10;
        CIRCLES_POS[0] = CONTAINER_WIDTH / 2.0 + CIRCLE_WIDTH / 2.0 + DIST_BETWEEN_CIRCLES;
        CIRCLES_POS[1] = CONTAINER_WIDTH / 2.0 - CIRCLE_WIDTH / 2.0;
        CIRCLES_POS[2] = CONTAINER_WIDTH / 2.0 + CIRCLE_WIDTH / 2.0 + DIST_BETWEEN_CIRCLES;
    }

    // https://www.raywenderlich.com/350-android-animation-tutorial-with-kotlin
    private void startAnimation(ArrayList<CircleView> circles) {

//        ValueAnimator positionAnimator = ValueAnimator.ofFloat(0f, circlesContainer.getWidth() / 2f);
//        positionAnimator.addUpdateListener(animation -> {
//            Float value = (Float) animation.getAnimatedValue();
//            circleView.setTranslationX(value);
//        });
//
//        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//
//        ObjectAnimator fadeAnimator = ObjectAnimator.ofFloat(circleView, "alpha", 0f, 1f);

        AnimatorSet set = new AnimatorSet();

//        set.play(positionAnimator).with(fadeAnimator);
        set.play(getPositionAnimator(circles.get(0), CONTAINER_WIDTH));//(float) CIRCLES_POS[0]));
        Log.e("anim", "CONTAINER WIDTH: " + CONTAINER_WIDTH);
        set.setDuration(1000);

//        set.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                Log.e("anim", "started");
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                Log.e("anim", "ended");
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });

        set.start();
    }

    private ValueAnimator getPositionAnimator(View view, float endPos) {
        ValueAnimator positionAnimator = ValueAnimator.ofFloat(0f, endPos);
        positionAnimator.addUpdateListener(animation -> {
            Float value = (Float) animation.getAnimatedValue();
            view.setTranslationX(value);
        });
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        return positionAnimator;
    }
}
