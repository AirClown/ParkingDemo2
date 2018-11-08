package com.example.yushichao.parkingdemo2;

/**
 * Created by yushi on 2018/11/5.
 */

public class Line {
    public int lineId;
    public int fromx;
    public int fromy;
    public int tox;
    public int toy;

    public Line(int id,int x1,int y1,int x2,int y2){
        this.lineId=id;
        this.fromx=x1;
        this.fromy=y1;
        this.tox=x2;
        this.toy=y2;
    }
}
