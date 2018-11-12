package com.example.yushichao.parkingdemo2;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class MagController {

    public float mag;
    private int nWindow = 20;
    private int nWindowVar = 200;
    private float[] values;
    private float[] mMag;
    private  float lastMag;

    private int count=0;

    private float[] speeds=new float[100];
    private int speed_count=0;

    private Timer timer;
    private TimerTask task;

    private MyFile file;

    private MagDetectorCallback callback;

    public float ind=35f;
    public int tim2=250;

    public interface MagDetectorCallback{
        void MagState(float var, float speed);
    }

    public MagController(MagDetectorCallback callback){
        values=new float[nWindow];
        mMag=new float[nWindowVar];

        this.callback=callback;

        timer=new Timer();
        task=new TimerTask() {
            @Override
            public void run() {
//                if (magnum>0) {
//                    calculateMag(mag / magnum);
//                }else{
//
//                }
                calculateMag(mag);
                //Log.e("Time",mag+"");
               // mag=magnum=0;
            }
        };
        timer.schedule(task,100,20);
    }

    private int magnum=0;
    public void refreshMag(float[] mags){
        mag=(float) Math.sqrt(mags[0]*mags[0]+mags[1]*mags[1]+mags[2]*mags[2]);
    }

    public void saveData(String path){//path=context.getExternalFilesDir(null)
        file=new MyFile(path,"Mag.txt");
        file.CreateFile();
    }

    private void calculateMag(float mag){
        if(mag==0){
            return;
        }

        for (int i=1;i<nWindow;i++){
            values[i-1] = values[i];
        }
        values[nWindow-1] = mag;

        float sumMag = 0.f;
        for (int i=0;i<nWindow;i++){
            sumMag += values[i];
        }
        sumMag /= nWindow;

        for (int i=1;i<nWindowVar;i++){
            mMag[i-1] = mMag[i];
        }
        mMag[nWindowVar-1] = sumMag;

        float v = Utils.var(mMag);
        float diff;
        if (v>1.f){
            diff = Math.abs(mMag[nWindowVar-nWindow]-mMag[nWindowVar-nWindow-1]);
            if (diff>0.6f){
                diff = lastMag;
            }
        }else{
            diff = 0.f;
        }

        lastMag = diff;

/*
        values[count]=mag;

        float[] data=new float[values.length];

        for(int i=0,j=count+1;i<values.length;i++,j++){
            if(j==values.length){
                j=0;
            }
            data[i]=values[j];
        }

        data=Utils.smoothFilter(data,30);

*/

      //  data2=Utils.smoothFilter(data2,50);
/*
        float[] data3=new float[var_num];

        for (int i=0;i<data3.length;i++){
            data3[i]=data2[data2.length-i-1];
        }

        float ave=0;
        float var=0;
        for(int i=0;i<data3.length;i++){
            ave+=data3[i];
        }
        ave/=data3.length;

        for(int i=0;i<data3.length;i++){
            var+=(data3[i]-ave)*(data3[i]-ave);
        }

        var=(float) Math.sqrt(var/data3.length);
*/


        speedCalculate(diff);

        if (++count==values.length){
            count=0;
        }
    }

    private void speedCalculate(float var){

        if(tim2>0) {
            tim2--;
            return;
        }

        speeds[speed_count]=var*ind;

        //float[] data=Utils.smoothFilter(speeds,5);

/*        if (callback!=null) {
            callback.MagState(var, data[speed_count]);
        }
        */
        if (callback!=null) {
            Log.e("MagValue",var+","+ind);
            callback.MagState(var, var*ind);
        }

        if (++speed_count==speeds.length){
            speed_count=0;
        }
    }
}
