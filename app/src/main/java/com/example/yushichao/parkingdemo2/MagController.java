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
    }

    public void refreshMag(float[] mags){
        mag=(float) Math.sqrt(mags[0]*mags[0]+mags[1]*mags[1]+mags[2]*mags[2]);
        calculateMag(mag);
    }

    public void saveData(String path){//path=context.getExternalFilesDir(null)
        file=new MyFile(path,"Mag.txt");
        file.CreateFile();
    }

    private float avesum=0;
    private float[] Mags=new float[nWindow];
    private float[] Mags_ave=new float[nWindowVar];
    private int count=0;
    private int count2=0;
    private void calculateMag(float mag){
        if (mag==0) return;

        Mags[count]=mag;

        if (Mags[Mags.length-1]!=0){
            float temp=Mags_ave[count2];
            Mags_ave[count2]=Utils.ave(Mags);
            avesum+=Mags_ave[count2];

            if(Mags_ave[Mags_ave.length-1]!=0){
                avesum-=temp;
                float var=Utils.var(Mags_ave,avesum/Mags_ave.length);

                float diff;
                if (var>1.f){
                    diff = Math.abs(Mags_ave[(nWindowVar+count2-nWindow)%nWindowVar]-
                            Mags_ave[(nWindowVar+count2-nWindow+1)%nWindowVar]);
                    if (diff>0.6f){
                        diff = lastMag;
                    }
                }else{
                    diff = 0.f;
                }

                lastMag = diff;

                speedCalculate(diff);
            }

            if (++count2==Mags_ave.length) count2=0;
        }

        if (++count==Mags.length) count=0;
    }

    private void calculateMag2(float mag){
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

        speedCalculate(diff);
    }

    private void speedCalculate(float var){

        if(tim2>0) {
            tim2--;
            return;
        }

        if (callback!=null) {
            Log.e("MagValue",var+","+ind);
            callback.MagState(var, var*ind);
        }
    }
}
