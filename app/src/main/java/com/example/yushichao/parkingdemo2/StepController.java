package com.example.yushichao.parkingdemo2;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yushi on 2018/11/5.
 */

public class StepController {

    //回调接口
    public interface StepCallback{
        void catchStep(int step);
    }

    //行走状态，WALK行走；STAY静止
    public static final int WALK=1;
    public static final int STAY=2;
    public static final int MOVE=3;

    //步数
    private int Step=0;
    //步长
    private float Length=0;
    //行走距离
    private float distance=0;

    //最新加速度值
    private float Acc;
    //保存之前的加速度个数
    private int Acc_num=100;
    //加速度保存的数组
    private float[] Accs;
    private int Acc_count=0;

    //人行状态计算保存数组
    private float[] Accs_state;

    //行走状态
    private int State;
    private long[] stateTime=new long[10];

    private boolean open=false;

    //回调函数
    private StepController.StepCallback mycallback;

    //需要传入回调函数，或者为null
    public StepController(StepCallback callback){
        mycallback=callback;

        Accs=new float[Acc_num];
        Accs_state=new float[20];
    }

    //得到计步数
    public int getStep(){
        return this.Step;
    }

    //得到步长
    public float getLength(){
        return this.Length;
    }

    //得到行走距离
    public float getDistance(){
        return this.distance;
    }

    //得到当前状态
    public int getState(){
        return this.State;
    }

    //传入加速度，为三轴加速度的标量和
    public void refreshAcceleration(float[] accs){
        this.Acc=(float)Math.sqrt(accs[0]*accs[0]+accs[1]*accs[1]+accs[2]*accs[2]);
        Detect(Acc);
    }

    //检测是否走了一步
    private void Detect(float acc){

        if(Acc_num>0){
            Accs[Acc_count]=acc;
            Accs_state[Acc_count%20]=acc;

            //状态更新
            if (Acc_count%20==0){
                UpdateState();
            }

            //检测是否走一步
            float[] data1=new float[Acc_num];
            float[] data2=new float[Acc_num];
            float[] data3=new float[Acc_num];
            float[] data4=new float[Acc_num];

            for(int i=0,j=Acc_count;i<Acc_num;i++,j--){
                if(j<0){
                    j+= Acc_num;
                }

                data1[i]=(Accs[j]);
            }

            data2=Utils.smoothFilter(data1,3);

//            data2[0]=(data1[0]+data1[1])/2;
//            data2[Acc_num-1]=(data1[Acc_num-1]+data1[Acc_num-2])/2;
//            for(int i=1;i<Acc_num-1;i++){
//                data2[i]=data1[i-1]+data1[i]+data1[i+1];
//                data2[i]/=3;
//            }

            float ave=Utils.ave(data2,0,data2.length);
            float ave_offset=0.8f;
//            for(int i=0;i<Acc_num;i++){
//                ave+=data2[i];
//            }
//            ave/=Acc_num;

            for (int i=1;i<Acc_num-1;i++){
                data3[i]=(data2[i]-data2[i+1])/20;
            }

            for(int i=1;i<Acc_num;i++){
                if(data3[i-1]*data3[i]<0){
                    if(data3[i]>0&&data2[i]>(ave+ave_offset)){
                        data4[i]=1;
                    }else if(data3[i]<0&&data2[i]<(ave-ave_offset)){
                        data4[i]=-1;
                    }
                }else if(data3[i]==0&&i<(Acc_num-1)){
                    if(data3[i-1]*data3[i+1]<0){
                        if(data2[i]>(ave+ave_offset)){
                            data4[i]=1;
                        }else if(data2[i]<(ave-ave_offset)){
                            data4[i]=-1;
                        }
                    }
                }
            }

            for(int i=0,j=0,sign=0;i<Acc_num;i++){
                if(data4[i]!=0){
                    if (sign==1&&data4[i]==1){
                        if(data2[i]>data2[j]){
                            data4[j]=0;
                            j=i;
                        } else{
                            data4[i]=0;
                        }
                    }else if(sign==-1&&data4[i]==-1){
                        if(data2[i]<data2[j]){
                            data4[j]=0;
                            j=i;
                        } else{
                            data4[i]=0;
                        }
                    }else{
                        sign=(int)data4[i];
                        j=i;
                    }
                }
            }

            int index=Acc_num/5;
            if(data4[index]<0){

                int up=index;
                int down=index;

                for(int i=index+1;i<Acc_num;i++){
                    if(data4[i]>0){
                        up=i;
                        break;
                    }
                }

                for(int i=up+1;i<Acc_num;i++){
                    if(data4[i]<0){
                        down=i;
                        break;
                    }
                }

                if(down-index>Acc_num/7&&down-index<Acc_num/1.5&&(2*data2[up]-data2[index]-data2[down])>6){
                    int sum=0;
                    for(int i=index+1;i<down;i++){
                        if(data2[i]>ave){
                            sum++;
                        }
                    }

                    //确定走步
                    if(sum>(down-index)/4.5) {
                        Step++;
                        //回调
                        if (mycallback!=null) {
                            mycallback.catchStep(Step);
                        }
                        //步长计算
                        DetectStepLength((down-index)*20,(2*data2[up]-data2[index]-data2[down])/2);
                    }
                }
            }

            if(++Acc_count==Acc_num){
                Acc_count=0;
            }
        }
    }

    //步长计算,该公式利用最小二乘法推导出,有一定可信性
    private void DetectStepLength(int time,float f){
        float steplength=0.35f-0.000155f*time+0.1638f*(float) Math.sqrt(f);
        this.Length=(this.Length+steplength)/2;
        distance+=steplength;
    }

    //行走状态更新,利用加速度方差方差
    private void UpdateState(){
        float var=Utils.var(Accs_state,0,Accs_state.length);
        //0.2~0.5为佳
        if (var<0.4){
            State=STAY;
        }else{
            State=MOVE;
        }
    }
}
