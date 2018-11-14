package com.example.yushichao.parkingdemo2;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yushi on 2018/11/13.
 */

public class AccController2 {

    //r为数据平滑窗口半径
    private final int r=100;
    private final int smooth=5;

    private float[] Accs;
    private float[] Accs_ave;
    private int[] sign;

    private float Accs_ave_sum=0;

    private int Accs_count=0;
    private int Accs_ave_count=0;
    private int sign_count=0;

    public interface AccCallback{
        //减速带检测，返回速度
        void BufferDetector(float speed);
    }
    private AccController2.AccCallback callback;

    public AccController2(AccController2.AccCallback callback){
        this.callback=callback;
        Accs=new float[smooth];
        Accs_ave=new float[r*2];
        sign=new int[r*2];
    }

    public void refreshAcc(float[] values){
        Accs[Accs_count]=(float) Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
        Accs[Accs_count]=Math.abs(Accs[Accs_count]-9.87f);

        if (Accs[Accs.length-1]!=0){
            float temp=Accs_ave[Accs_ave_count];
            Accs_ave[Accs_ave_count]=Utils.ave(Accs);
            Accs_ave_sum+=Accs_ave[Accs_ave_count];

            if (Accs_ave[Accs_ave.length-1]!=0){
                Accs_ave_sum-=temp;

                float ave=Accs_ave_sum/Accs_ave.length;
                float var=Utils.var(Accs_ave,ave);
                float data=Accs_ave[(Accs_ave_count+r)%(2*r)];

                if (data>1.5&&data>2*ave&&var*3>ave&&data>Accs_ave[(Accs_ave_count+r-1)%(2*r)]){
                    sign[sign_count]=1;
                }else{
                    sign[sign_count]=0;
                }

                if (sign[(sign_count+r/2)%(2*r)]>0){
                    List<Point> points = new ArrayList<>();

                    for (int i=0;i<sign.length;++i){
                        if (sign[i]>0){
                            points.add(new Point((sign_count+r*2+i)%(2*r),0));
                        }
                    }

                    if (points.size() > 2) {
                        Point[] re = Utils.Kmeans(2, points, 20);
                        if (re != null) {

                            //0.02是传感器返回时间，3.6作为m/s和km/h的转换
                            float speed = (float) (3 / (Math.abs(re[0].x - re[1].x) * 0.02) * 3.6);

                            if (speed < 30 && speed > 3) {
                                callback.BufferDetector(speed);
                            }
                        }
                    }
                }

                if (++sign_count==sign.length) sign_count=0;
            }

            if (++Accs_ave_count==Accs_ave.length) Accs_ave_count=0;
        }

        if (++Accs_count==Accs.length) Accs_count=0;
    }
}
