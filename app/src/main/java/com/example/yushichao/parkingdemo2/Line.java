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

    public float lineangle;

    public Line(int id,int x1,int y1,int x2,int y2){
        this.lineId=id;
        this.fromx=x1;
        this.fromy=y1;
        this.tox=x2;
        this.toy=y2;

        if(fromx==tox){
            lineangle=(float)Math.PI/2*((fromy-toy)>0?1:-1);
        }else{
            lineangle=(float)Math.atan((fromy-toy)/(fromx-tox));
        }
    }
}
