package com.unagit.parkedcar.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.unagit.parkedcar.R;

public class CircleView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int color;
    private int size = 30; // default size in px

    public CircleView(Context context) {
        super(context);
        color = getResources().getColor(R.color.colorAccent);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setContentDescription("Getting parking location is in progress");

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        size = getMeasuredHeight();
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, paint);
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_HOVER_ENTER:
                Log.d("test", "onHoverEvent: ACTION_HOVER_ENTER");
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                return true;
        }
        return super.onHoverEvent(event);
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER) {
            Log.d("test", "onPopulateAccessibilityEvent");
            CharSequence text = "this is a test";
            Log.d("test", "text before: " + event.getText());
            event.getText().add(text);
            Log.d("test", "text after: " + event.getText());
        }
    }
}
