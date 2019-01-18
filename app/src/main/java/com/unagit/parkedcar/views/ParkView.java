package com.unagit.parkedcar.views;

import android.content.Context;
import android.util.AttributeSet;
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
    }
}
