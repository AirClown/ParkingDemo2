package com.example.yushichao.parkingdemo2;

import android.graphics.Point;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by yushi on 2018/11/7.
 */

public class Navigation {

    public interface NavigationCallback{
        void Position(int x,int y);
        void Orientation(float angle);
    }

    //真实坐标
    private int Rx,Ry;

    //映射坐标
    public int Mx,My;

    //灯光信息
    private Lamp prelamp;
    private long lamptime;

    //速度信息
    private float lampSpeed;
    private float magSpeed;
    private float bufferSpeed;
    public float mixSpeed;

    //角度信息
    private float gyrangle;
    public float mixangle;

    //拓扑信息
    private List<Line> lines;
    private List<Lamp> lamps;

    //回调
    private NavigationCallback callback;

    //定时器
    private Timer timer;
    private TimerTask task;


    public Navigation(NavigationCallback call) {
        this.callback=call;

        //获取拓扑数据
        lines=Utils.getLine();
        lamps=Utils.getLamp();

        //初始化位置
        InitPosition(0,0);

        //开启定时器，一秒一更新
        timer=new Timer();
        task=new TimerTask() {
            @Override
            public void run() {
                positionPrediction();
            }
        };
        timer.schedule(task,500,1000);
    }

    //初始化位置
    public void InitPosition(int x,int y){
        if (x==0||y==0){
            Rx=lamps.get(0).x;
            Ry=lamps.get(0).y;
        }else{
            Rx=x;
            Ry=y;
        }
        Mapping(Rx,Ry);
    }

    //坐标映射
    private void Mapping(int x,int y){
        int min=-1;
        Point foot=new Point();
        for (Line l:lines){
            Point p=Utils.getFoot(l,x,y);
            int d=(int)Math.sqrt((p.x-x)*(p.x-x)+(p.y-y)*(p.y-y));
            if(min<0||min>d){
                min=d;
                foot.set(p.x,p.y);
            }
        }

        Mx=foot.x;
        My=foot.y;
    }

    //更新陀螺仪角度
    public void refreshGyroscopeAngle(float angle,float speed){
        gyrangle=angle;
        angleFusion();
    }

    //更新手机三轴方向角
    public void refreshOrientation(float[] value){
        
    }

    //更新磁场估计速度
    public void refreshMagSpeed(float speed){
        Log.e("magspeed",mixSpeed+"");
        magSpeed=(magSpeed+speed)/2;
        speedFusion();
    }

    //更新缓冲带速度估计
    public void refreshBufferSpeed(float speed){
        bufferSpeed=speed;
        speedFusion();
    }

    //更新灯
    public int refreshLamp(int lampid){
        Lamp lamp=null;
        for (Lamp l:lamps){
            if (lampid==l.lampId){
                lamp=l;
                break;
            }
        }

        if (lamp!=null){

            Rx=lamp.x;
            Ry=lamp.y;

            if (prelamp==null||prelamp.lampId==lamp.lampId){
                lamptime=System.currentTimeMillis();
            }else{
                long t=System.currentTimeMillis()-lamptime;
                lampSpeed=0.001f*(Math.abs(prelamp.x-lamp.x)+Math.abs(prelamp.y-lamp.y))/t;
                prelamp=lamp;
                speedFusion();
            }
        }

        return 0;
    }

    //速度融合
    private void speedFusion(){
        mixSpeed=(magSpeed  );
//        int count=0;
//        if (magSpeed!=0){
//            mixSpeed+=magSpeed;
//            ++count;
//        }
//
//        if (bufferSpeed!=0){
//            mixSpeed+=bufferSpeed;
//            ++count;
//        }
//
//        if (lampSpeed!=0){
//            mixSpeed+=lampSpeed;
//            ++count;
//        }
//
//        if (count>1) {
//            mixSpeed /= count;
//        }
    }

    //角度融合
    private void angleFusion(){
        mixangle=gyrangle;
    }

    //位置推演
    private void positionPrediction(){
        Rx+=10f*mixSpeed*Math.sin(mixangle);
        Ry-=10f*mixSpeed*Math.cos(mixangle);
        Mapping(Rx,Ry);
        callback.Position(Mx,My);
    }

    public void refreshStep(float steplength,float state){
        Rx+=10f*steplength*Math.sin(mixangle);
        Ry-=10f*steplength*Math.cos(mixangle);
        Mapping(Rx,Ry);
    }
}
