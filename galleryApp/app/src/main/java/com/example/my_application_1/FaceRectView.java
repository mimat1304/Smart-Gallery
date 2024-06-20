package com.example.my_application_1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class FaceRectView extends View {
    private Paint paint;
    private ArrayList<Rect> rects = new ArrayList<>();

    public FaceRectView(Context context) {
        super(context);
        init();
    }

    public FaceRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
    }

    public void setRects(ArrayList<Rect> rects) {
        this.rects = rects;
        invalidate();  // Trigger a redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Rect rect : rects) {
            canvas.drawRect(rect, paint);
        }
    }
}