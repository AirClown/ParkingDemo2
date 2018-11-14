package com.example.yushichao.parkingdemo2;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yushi on 2018/9/19.
 */

public class Utils {

    //拓扑图数据微调参数
    private static int Indx=250;
    private static int Indy=250;

    //获取路径拓扑
    public static List<Line> getLine(){
        List<Line> lines=new ArrayList<>();

        Line line1=new Line(1,266,345,266,957);
        lines.add(line1);

        Line line2=new Line(2,line1.tox,line1.toy,428,957);
        lines.add(line2);

        Line line3=new Line(3,line1.fromx,line1.fromy,428,345);
        lines.add(line3);

        Line line4=new Line(4,line3.tox,line3.toy,line2.tox,line2.toy);
        lines.add(line4);

        Line line5=new Line(5,line4.tox,line4.toy,588,957);
        lines.add(line5);

        Line line6=new Line(6,line5.tox,line5.toy,588,1514);
        lines.add(line6);

        Line line7=new Line(7,267,1514,line6.tox,line6.toy);
        lines.add(line7);

        Line line8=new Line(8,118,1514,line7.tox,line7.toy);
        lines.add(line8);

        Line line9=new Line(9,line8.fromx,line8.fromy,118,2129);
        lines.add(line9);

        Line line10=new Line(10,line9.tox,line9.toy,267,2129);
        lines.add(line10);

        Line line11=new Line(11,line7.fromx,line7.fromy,267,1904);
        lines.add(line11);

        Line line12=new Line(12,line11.tox,line11.toy,362,1904);
        lines.add(line12);

        Line line13=new Line(13,line11.tox,line11.toy,line10.tox,line10.toy);
        lines.add(line13);

        for(Line line:lines){
            line.fromx+=Indx;
            line.fromy-=Indy;
            line.tox+=Indx;
            line.toy-=Indy;
        }

        return lines;
    }

    //获取灯拓扑
    public static List<Lamp> getLamp(){
        List<Lamp> lamps=new ArrayList<>();

        Lamp lamp1=new Lamp(1,175,2129,10);
        lamps.add(lamp1);

        Lamp lamp2=new Lamp(2,267,2095,13);
        lamps.add(lamp2);

        Lamp lamp3=new Lamp(3,267,1913,11);
        lamps.add(lamp3);

        Lamp lamp4=new Lamp(4,267,1675,11);
        lamps.add(lamp4);
//
        Lamp lamp5=new Lamp(5,171,1514,8);
        lamps.add(lamp5);
//
        Lamp lamp6=new Lamp(6,118,1676,8);
        lamps.add(lamp6);

        Lamp lamp7=new Lamp(7,118,1840,9);
        lamps.add(lamp7);

        Lamp lamp8=new Lamp(8,118,2005,9);
        lamps.add(lamp8);


//
//        Lamp lamp8=new Lamp(118,2005);
//        lamps.add(lamp8);
//
//        Lamp lamp9=new Lamp(331,1514);
//        lamps.add(lamp9);
//
//        Lamp lamp10=new Lamp(451,1514);
//        lamps.add(lamp10);
//
//        Lamp lamp11=new Lamp(588,1447);
//        lamps.add(lamp11);
//
//        Lamp lamp12=new Lamp(588,1278);
//        lamps.add(lamp12);
//
//        Lamp lamp13=new Lamp(588,994);
//        lamps.add(lamp13);
//
//        Lamp lamp14=new Lamp(428,828);
//        lamps.add(lamp14);
//
//        Lamp lamp15=new Lamp(428,553);
//        lamps.add(lamp15);
//
//        Lamp lamp16=new Lamp(428,404);
//        lamps.add(lamp16);
//
//        Lamp lamp17=new Lamp(320,345);
//        lamps.add(lamp17);
//
//        Lamp lamp18=new Lamp(266,510);
//        lamps.add(lamp18);
//
//        Lamp lamp19=new Lamp(266,756);
//        lamps.add(lamp19);
//
//        Lamp lamp20=new Lamp(266,940);
//        lamps.add(lamp20);
//
//        Lamp lamp21=new Lamp(428,940);
//        lamps.add(lamp21);

        for(Lamp lamp:lamps){
            lamp.x+=Indx;
            lamp.y-=Indy;
        }
        return lamps;
    }

    //均值平滑滤波器
    public static float[] smoothFilter(float[] data,int strength){
        if (strength>data.length/2){
            strength=data.length/2;
        }

        float[] back=new float[data.length];

        back[0]=data[0];
        for (int i=1;i<data.length;i++){
            if (i<strength){
                back[i]=back[i-1]+data[i];
            }else{
                back[i]=back[i-1]-data[i-strength]+data[i];
            }
        }

        for (int i=1;i<back.length;i++){
            if (i<strength){
                back[i]/=(i+1);
            }else{
                back[i]/=strength;
            }
        }

        return back;
    }

    //差分
    public static float[] diff(float[] values){
        float[] data=new float[values.length-1];

        for(int i=0;i<data.length;i++){
            data[i]=values[i]-values[i+1];
        }

        return data;
    }

    //绝对值
    public static void abs(float[] values){
        for (int i=0;i<values.length;++i){
            values[i]=Math.abs(values[i]);
        }
    }

    //最大值
    public static float max(float[] values){
        if (values.length==0) return 0;
        float max=values[0];
        for (float x:values){
            if (x>max) max=x;
        }
        return max;
    }

    //最小值
    public static float min(float[] values){
        if (values.length==0) return 0;
        float min=values[0];
        for (float x:values){
            if (x<min) min=x;
        }
        return min;
    }

    //均值
    public static float ave(float[] values,int start,int end){
        if (start<0||start>=end) Log.e("Utils ave Error","Index byand");
        if (end>values.length) end=values.length;

        float re=0;
        for (int i=start;i<end;i++){
            re+=values[i];
        }

        return re/(end-start);
    }

    //方差
    public static float var(float[] values,int start,int end){
        if (start<0||start>=end) Log.e("Utils ave Error","Index byand");
        if (end>values.length) end=values.length;

        float re=0;
        float ave=ave(values,start,end);
        for (int i=start;i<end;i++){
            re+=(values[i]-ave)*(values[i]-ave);
        }

        return re/(end-start);
    }

    //点到直线垂足
    public static Point getFoot(Line line, int x, int y){
        Point foot=new Point();

        float dx=line.fromx-line.tox;
        float dy=line.fromy-line.toy;

        float u=(x-line.fromx)*dx+(y-line.fromy)*dy;
        u/=dx*dx+dy*dy;

        foot.x=(int)(line.fromx+u*dx);
        foot.y=(int)(line.fromy+u*dy);

        float d=Math.abs((line.tox-line.fromx)*(line.tox-line.fromx)+(line.toy-line.fromy)*(line.toy-line.fromy));
        float d1=Math.abs((foot.x-line.fromx)*(foot.x-line.fromx)+(foot.y-line.fromy)*(foot.y-line.fromy));
        float d2=Math.abs((foot.x-line.tox)*(foot.x-line.tox)+(foot.y-line.toy)*(foot.y-line.toy));

        if(d1>d||d2>d){
            if (d1<d2)  return new Point(line.fromx,line.fromy);
            else return new Point(line.tox,line.toy);
        }

        return foot;
    }

    //Kmeans算法
    //k:聚类数量
    //points:坐标数据
    //iteration:迭代计算次数
    //返回：聚类中心
    public static Point[] Kmeans(int k, List<Point> points, int iteration){
        if (points.size()<k) return null;

        //聚类中心
        Point[] centre=new Point[k];
        for(int i=0;i<centre.length;i++){
            int index=(int)(Math.random()*points.size());
            centre[i]=new Point(points.get(index).x,points.get(index).y);
        }

        while (--iteration>0){
            //分组下标，每个数据对应聚类中心数组的下标
            int[] index=new int[points.size()];

            //统计每组的数据数量
            int[] count=new int[centre.length];

            //分组
            for (int i=0;i<points.size();i++){
                //最小距离，-1代表没有初值
                float min=-1;

                //寻找最近的聚类中心
                for (int j=0;j<centre.length;j++){
                    float d=(float) Math.sqrt((centre[j].x-points.get(i).x)*(centre[j].x-points.get(i).x)+
                            (centre[j].y-points.get(i).y)*(centre[j].y-points.get(i).y));
                    if(min<0||d<min){
                        min=d;
                        index[i]=j;
                    }
                }

                ++count[index[i]];
            }

            //更新聚类中心
            for (int i=0;i<index.length;i++){
                centre[index[i]].x+=points.get(i).x;
                centre[index[i]].y+=points.get(i).y;
            }

            for(int i=0;i<centre.length;i++){
                if (count[i]==0) continue;
                centre[i].x/=(count[i]+1);
                centre[i].y/=(count[i]+1);
            }
        }

        return centre;
    }

    public static float var(float[] values){
        float m = 0.f,v = 0.f;

        for(int i=0;i<values.length;i++){
            m += values[i];
        }
        m /= values.length;

        for(int i=0;i<values.length;i++){
            v += (values[i]-m)*(values[i]-m);
        }
        v /= values.length;

        return v;
    }

    public static float var(float[] value,float ave){
        float re=0;

        for (int i=0;i<value.length;i++){
            re+=(value[i]-ave)*(value[i]-ave);
        }

        return re;
    }

    public static float ave(float[] value){
        float re=0;
        for (int i=0;i<value.length;++i){
            re+=value[i];
        }
        return re/value.length;
    }

    private static int topo[] = { 0, 57,203,365,603,860,1075,1239,1404,1528};
    public int[] test(int x,int y){
        x-=Indx;
        y+=Indy;

        int d=x-118+2129-y;

        if (!(x==267||y==2129)){
            d=topo[topo.length-1]-d;
        }

        int[] re=new int[topo.length-2];
        for (int i=1;i<topo.length-1;++i){
            int x1=Math.abs(topo[i]-d);
            int x2=topo[topo.length-1]-d+topo[i];

            re[i-1]=Math.min(x1,x2);
        }

        return re;
    }

}
