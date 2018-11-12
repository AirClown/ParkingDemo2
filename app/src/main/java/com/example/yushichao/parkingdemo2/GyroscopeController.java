package com.example.yushichao.parkingdemo2;

import android.renderscript.Sampler;

/**
 * Created by LiteShare on 2018/11/9.
 */

public class GyroscopeController {

    private static final int TURN_LEFT=-1;
    private static final int TURN_RIGHT=1;

    private float angle;
    private long time;
    private float speed;

    //拐弯判定
    private long check_time;
    private float  check_angle;

    private MyFile file;

    public interface GyroscopeControllerCallback {
        void refreshGyr(float angle,float speed);
        void turnDetecte(int state);
    }
    private GyroscopeControllerCallback callback;

    public GyroscopeController(GyroscopeControllerCallback callback){
        this.callback=callback;
        angle=0;
        speed=0;
        check_angle=0;
        time=0;
        check_time=0;
    }

    //保存数据
    public void saveData(String path){
        file=new MyFile(path,"gry.txt");
        file.CreateFile();
    }

    //角度校准
    public void correctAngle(float value){
        this.angle=value;
    }

    public void refreshGyroscope(float[] values){
        if (time==0||Math.abs(values[2])<0.05){
            time=System.currentTimeMillis();
            check_time=time;
            return;
        }

        if (file!=null){
            file.WriteIntoFile(""+ values[2]);
        }

        speed=values[2];

        long t=System.currentTimeMillis()-time;

        angle+=0.001f*speed*t;

        time+=t;

        callback.refreshGyr(angle,values[2]);

        if (time-check_time>5000){
            if (Math.abs(angle-check_angle)>Math.toRadians(70)){
                callback.turnDetecte(angle>check_angle?TURN_LEFT:TURN_RIGHT);
            }

            check_time=time;
            check_angle=angle;
        }
    }
}
