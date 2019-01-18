package com.unagit.parkedcar.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.unagit.parkedcar.R;

public class CircleView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int color;
    private int size = 30; // default size in px

    public CircleView(Context context) {
        super(context);
        color = getResources().getColor(R.color.colorAccent);
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
}
