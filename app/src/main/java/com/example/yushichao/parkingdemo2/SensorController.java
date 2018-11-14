package com.example.yushichao.parkingdemo2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by yushi on 2018/10/25.
 */

public class SensorController implements SensorEventListener {

    private SensorManager manager;

    public interface SensorCallback{
        //加速度
        void refreshAcc(float[] accs);

        //磁场
        void refreshMag(float[] mags);

        //方向
        void refreshOri(float[] angles);

        //陀螺仪
        void refreshGyr(float[] values);
    }

    private SensorCallback callback;

    public SensorController(SensorManager manager, SensorCallback callback){
        this.manager=manager;
        this.callback=callback;
    }

    //注册传感器
    public boolean registerSensor(int type,int speed){
        if (manager==null) return false;

        Sensor sensor=manager.getDefaultSensor(type);
        manager.registerListener(this,sensor,speed);
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (callback==null) return;
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                callback.refreshAcc(event.values.clone());
                break;
            case Sensor.TYPE_GYROSCOPE:
                callback.refreshGyr(event.values.clone());
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                //Log.e("MagValue",event.values[0]+","+event.values[1]+","+event.values[2]);
                callback.refreshMag(event.values.clone());
                break;
            case Sensor.TYPE_ORIENTATION:
                callback.refreshOri(event.values.clone());
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
