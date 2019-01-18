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

import androidx.annotation.Nullable;

public class ParkView extends LinearLayout {
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
        FrameLayout circlesContainer = findViewById(R.id.circles_container);
        CircleView circleView = new CircleView(getContext());
        circlesContainer.addView(circleView);
        Button btn = findViewById(R.id.park_car);
        btn.setOnClickListener(v -> animate(circlesContainer, circleView));
//        animate(circlesContainer, circleView);
    }

    // https://www.raywenderlich.com/350-android-animation-tutorial-with-kotlin
    private void animate(FrameLayout circlesContainer, CircleView circleView) {
        ValueAnimator positionAnimator = ValueAnimator.ofFloat(0f, circlesContainer.getWidth() / 2f);
        positionAnimator.addUpdateListener(animation -> {
            Float value = (Float) animation.getAnimatedValue();
            circleView.setTranslationX(value);
        });

        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator fadeAnimator = ObjectAnimator.ofFloat(circleView, "alpha", 0f, 1f);

        AnimatorSet set = new AnimatorSet();

        set.play(positionAnimator).with(fadeAnimator);
        set.setDuration(1000);

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.e("anim", "started");

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.e("anim", "ended");
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


}
