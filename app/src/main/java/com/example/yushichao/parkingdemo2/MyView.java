package com.example.yushichao.parkingdemo2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class MyView extends View {

    private int Range;

    private int count;
    private float[] data;

    private String title="";

    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRange(int num){
        Range=num;

        count=0;
        data=new float[Range];
    }

    public void setData(float num){
        if (Range<=0){
            Log.e("Error","Please setRange first.");
            return;
        }

        data[count]=num;

        if (++count==Range){
            count=0;
        }

        invalidate();
    }

    public void setTitle(String title){
        this.title=title;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(Range>0) {
            int Width=canvas.getWidth();
            int Height=canvas.getHeight();

            float max=1;
            //float[] data=Utils.smoothFilter(this.data,40);
            float[] data0=new float[Range];
            for(int i=0,j=count-1;i<Range;++i,--j){
                if (j<0){
                    j+=Range;
                }

                data0[i]=data[j];

                if(data0[i]>max){
                    max=data0[i];
                }
            }


            Paint paint1 = new Paint();
            paint1.setColor(Color.BLACK);
            paint1.setStrokeWidth(5);
            paint1.setTextSize(50);
            paint1.setStyle(Paint.Style.STROKE);

            canvas.drawLine(Width/2,0,Width/2,Height,paint1);
            canvas.drawLine(0,0,0,Height,paint1);
            canvas.drawText(title,0,50,paint1);

            paint1.setColor(Color.RED);
            paint1.setStrokeWidth(10);

            float ix=(float)(Width-Width/20)/max;
            float iy=(float)Height/Range;

            Path path=new Path();
            path.moveTo(data0[0]*ix,0);
            for(int i=1;i<Range;i++){
                if (data0[i]>=0) {
                    path.lineTo(data0[i] * ix, iy * i);
                }
            }
            canvas.drawPath(path,paint1);
        }
    }
}
