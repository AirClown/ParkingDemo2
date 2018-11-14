package com.example.yushichao.parkingdemo2;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yushi on 2018/10/25.
 */

public class AccController {

    //r为数据平滑窗口半径
    private int r=100;

    private int dataNum=4*r;

    private int count;
    private float[] Accs;

    public interface AccCallback{
        //减速带检测，返回速度
        void BufferDetector(float speed);
    }
    private AccCallback callback;

    private MyFile file;

    public AccController(AccCallback callback){
        this.callback=callback;

        Accs=new float[dataNum];
        count=0;
    }

    //保存数据
    public void savaData(String path){
        file=new MyFile(path,"acc.txt");
        file.CreateFile();
    }

    //信号处理
    public void refreshAcc(float[] values) {
        Accs[count] = (float) Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);

        if (file!=null){
            file.WriteIntoFile(Accs[count]+"");
        }

        float[] data = new float[Accs.length];
        for (int i = 0, j = count; i < Accs.length; i++, j--) {
            if (j < 0) {
                j = Accs.length - 1;
            }
            data[i] = Accs[j];
        }

        //简单平滑，5为佳
        data=Utils.smoothFilter(data,5);

        //均值方差作为背景噪声的两个重要指标，以所在点为中心，r为窗口半径进行计算
        float[] ave=new float[data.length];
        float[] var=new float[data.length];

        float sum=0;
        for (int i=0;i<data.length;i++){
            if (i<2*r){
                 sum+=data[i];
            }else{
                sum+=data[i];
                sum-=data[i-2*r];

                ave[i-r]=sum/(2*r);
                float v=0;
                for (int j=i-2*r+1;j<=i;j++){
                    v+=(data[j]-ave[i-r])*(data[j]-ave[i-r]);
                }
                var[i-r]=v/(2*r);
            }
        }

        //抓取有用信号
        float[] sign=new float[data.length];
        for (int i=r;i<data.length-r;i++){
            if (data[i]>1.5&&data[i]>2*ave[i]&&var[i]*3>ave[i]){
                sign[i]=5;
            }
        }

        //利用K-means算法提取信号中心点，作为速度评估的参数
        if (sign[r*2]>0&&sign[r]==0&&sign[data.length-r-1]==0) {
            List<Point> points = new ArrayList<>();
            for (int i = r; i < data.length - r; i++) {
                if (sign[i] > 0 && data[i] > data[i - 1] && data[i] > data[i + 1]) {
                    points.add(new Point(i, 0));
                }
            }

            if (points.size() > 2) {
                Point[] re = Utils.Kmeans(2, points, 20);
                if (re != null) {
                    sign[re[0].x] = 10;
                    sign[re[1].x] = 10;

                    //0.02是传感器返回时间，3.6作为m/s和km/h的转换
                    float speed = (float) (3 / (Math.abs(re[0].x - re[1].x) * 0.02) * 3.6);

                    if (speed < 30 && speed > 3) {
                        callback.BufferDetector(speed);
                    }
                }
            }
        }
        count=(count+1)%Accs.length;
    }
}
