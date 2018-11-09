package com.example.yushichao.parkingdemo2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

public class Map extends View{

    private float MapRadio=0;

    //地图拓扑数据
    private List<Line> lines;
    private List<Lamp> lamps;

    //文字信息
    private String text="";

    //角度信息
    private float angle;

    //坐标信息
    private int X;
    private int Y;

    public Map(Context context) {
        super(context);
    }

    public Map(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Map(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //设置路径拓扑图
    public void setLine(List<Line> list){
        this.lines=list;
    }

    //设置灯光拓扑图
    public void setLamp(List<Lamp> list){
        this.lamps=list;
    }

    //设置文字信息
    public void setText(String text){
        this.text=text;
    }

    //追加文字信息
    public void addText(String text){
        this.text+=text;
    }

    //设置位置信息
    public void setPosition(int x,int y){
        this.X=x;
        this.Y=y;
    }

    //设置角度信息
    public void setAngle(float angle){
        this.angle=(float) Math.PI*angle/180;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (MapRadio==0){
            MapRadio=0.000677f*(float) Math.sqrt(canvas.getHeight()*canvas.getWidth());
        }

        DrawLine(canvas);
        DrawLamp(canvas);
        DrewText(canvas);
        DrawPosition(canvas);
    }

    //绘制位置
    private void DrawPosition(Canvas canvas){
        if (X!=0&&Y!=0){
            Paint paint = new Paint();
            paint.setColor(Color.argb(255,255,69,0));
            paint.setStrokeWidth(5);
            paint.setAlpha(150);

            if (angle!=0) {
                int x = (int) (X + 30 * Math.sin(angle) / MapRadio);
                int y = (int) (Y - 30 * Math.cos(angle) / MapRadio);

                canvas.drawLine(MapRadio * X, MapRadio * Y, MapRadio * x, MapRadio * y, paint);
            }

            canvas.drawCircle(MapRadio*X,MapRadio*Y,20,paint);
        }else {
            Log.e("MapError","No Position");
        }
    }

    //绘制灯
    private void DrawLamp(Canvas canvas){
        if (lamps!=null&&lamps.size()>0){
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(10);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAlpha(200);

            for (Lamp lamp:lamps){
                canvas.drawCircle(MapRadio*lamp.x,MapRadio*lamp.y,20,paint);
            }
        }else{
            Log.e("MapError","No Lamp");
        }
    }

    //绘制路径
    private void DrawLine(Canvas canvas){

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(10);

        //绘制背景
        canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),paint);

        if (lines!=null&&lines.size()>0){
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.GRAY);
            for (Line line:lines){
                canvas.drawLine(MapRadio*line.fromx,MapRadio*line.fromy,
                        MapRadio*line.tox,MapRadio*line.toy,paint);
            }
        }else{
            Log.e("MapError","No Line");
        }
    }

    //绘制文字信息
    private void DrewText(Canvas canvas){
        if (text!=null) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setTextSize(80);
            paint.setAlpha(100);
            canvas.drawText(text, 0, 300, paint);
        }
    }
}
