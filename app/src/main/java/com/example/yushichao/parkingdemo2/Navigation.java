package com.example.yushichao.parkingdemo2;

import android.graphics.Point;

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
    }

    //真实坐标
    private int Rx,Ry;

    //映射坐标
    private int Mx,My;

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
                callback.Position(Mx,My);
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
}
