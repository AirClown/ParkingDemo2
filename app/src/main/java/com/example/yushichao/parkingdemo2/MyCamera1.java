package com.example.yushichao.parkingdemo2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by yushi on 2018/9/3.
 */

public class MyCamera1 {
    //camera1 API
    private Camera camera;
    private Camera.Parameters parameters;

    //Ui
    private SurfaceView surfaceView;
    private SurfaceHolder holder;

    //context
    private Activity activity;

    //拍照锁
    private boolean TAKING=false;

    //相机参数
    private  int count=0;
    private boolean isGetSamp = false;

    //前置摄像头1和后置摄像头0
    private int cameraPosition;

    //回调
    public interface Camera1Callback{
        void UpdateText(String text);

        void UpdateImage(Bitmap bitmap);

        int UpdateLamp(int LampId);
    }

    private Camera1Callback callback;

    public MyCamera1(Activity activity, SurfaceView surfaceView, Camera1Callback callback, int cameraPosition){
        this.activity=activity;
        this.surfaceView=surfaceView;
        this.callback=callback;
        this.cameraPosition=cameraPosition;
    }

    public void openCamera(){
        holder=surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                StartCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void StartCamera(){
        try {
            camera= Camera.open(cameraPosition);

            if (parameters==null){
                parameters=camera.getParameters();
                parameters.set("zsl","off");
                parameters.set("auto-exposure-lock","true");
                parameters.set("hw-professional-mode","on");

                //ISO
                parameters.set("hw-sensor-iso","1600");
                //快门
                parameters.set("hw-sensor-exposure-time","0.0008");
                List<Camera.Size> list= parameters.getSupportedPictureSizes();
                Camera.Size max=null;
                for(int i=0;i<list.size();i++){
                    if(400<list.get(i).height&&list.get(i).height<640){
                        max=list.get(i);
                        break;
                    }
                }
                parameters.setPictureSize(max.width,max.height);
                //parameters.setRotation(90);
            }
            camera.setParameters(parameters);

            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            //camera.setPreviewCallback(previewCallback);
            camera.startPreview();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //开始拍照
    public void TakePhoto(){
        if (!TAKING&&camera!=null) {
            TAKING=true;
            camera.takePicture(null, null, pictureCallback);
        }
    }

    private Camera.PictureCallback pictureCallback=new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.stopPreview();
            Log.e("Takepicture","take");
            Camera.Size size = camera.getParameters().getPreviewSize();
            try {
                //保存图片
                //SavePicture(data);

                //获得图片的像素
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                //后续处理
                getXY(mean(bitmap));
            } catch (Exception ex) {
                Log.e("Sys", "Error:" + ex.getMessage());
            }

            camera.startPreview();
            TAKING=false;
        }
    };

    private int previewCount=0;
    private Camera.PreviewCallback previewCallback=new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Size size = camera.getParameters().getPreviewSize();
            previewCount++;
            try {
                YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                if (image != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                    stream.close();
                }
            } catch (Exception ex) {
                Log.e("Sys", "Error:" + ex.getMessage());
            }
        }
    };

    private float[] mean(Bitmap bitmap) {
        int height=bitmap.getWidth();
        int width=bitmap.getHeight();
        int[] pixels=new int[width*height];
        bitmap.getPixels(pixels,0,height,0,0,height,width);
        int step = 4;
        int lenY2 = height/step;
        float[] y = new float[width];
        int red,green,blue;

        for (int i = 0; i < width; i++) {
            red = 0;
            green = 0;
            blue = 0;
            for (int j = 0; j < height; j+=step) {
                red += ((pixels[i * width + j] & 0x00FF0000) >> 16);
                green += ((pixels[i * width + j] & 0x0000FF00) >> 8);
                blue += (pixels[i * width + j] & 0x000000FF);
            }
            //y[width - i - 1] = ((float)red*0.3f+(float)green*0.59f+(float)blue*0.11f) / (float) (lenY2);
            y[i] = ((float)red*0.3f+(float)green*0.59f+(float)blue*0.11f) / (float) (lenY2);
        }
        return y;
    }

    private void SavePicture(byte[] bytes){
        String addr=activity.getExternalFilesDir(null)+"/";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        addr+=  formatter.format(System.currentTimeMillis());
        File file=new File(addr+".jpg");

        try{
            FileOutputStream output = new FileOutputStream(file);
            output.write(bytes);
            Toast.makeText(activity, "图片保存在："+addr, Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getXY(final float[] data){
        Log.i("getXY","xy");
        count++;


//        if (isGetSamp){
//            float optNumSamp = lightComm.getNumSamp(data);
//            callback.UpdateText(count+":"+optNumSamp);
//
//        }else{
//            long address = lightComm.getAddress(data);
//            callback.UpdateText(""+address);
//            previewCount=0;
//            if(address<100&&address>=0) {
//                callback.UpdateLamp((int) address);
//            }
//        }
    }


}
