package com.wztechs.remo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
public class MyCanvas extends View {


    public MyCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
            int color = Color.rgb(250, 250, 250);
        canvas.drawColor(Color.BLUE);
    }

}
