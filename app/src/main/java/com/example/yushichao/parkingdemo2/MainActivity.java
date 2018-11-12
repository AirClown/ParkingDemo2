package com.example.yushichao.parkingdemo2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //APP所需权限
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //传感器
    private SensorController sensorController;
    private SensorController.SensorCallback sensorCallback=new SensorController.SensorCallback() {
        @Override
        public void refreshAcc(float[] accs) {
            accController.refreshAcc(accs);
            //stepController.refreshAcceleration(accs);
        }

        @Override
        public void refreshMag(float[] mags) {
            magController.refreshMag(mags);
        }

        @Override
        public void refreshOri(float[] angles) {
            if (navigation!=null){
                navigation.refreshOrientation(angles);
            }
        }

        @Override
        public void refreshGyr(float[] values) {
            gyroscopeController.refreshGyroscope(values);
        }
    };

    //计步控制器
    //private StepController stepController;
//    private StepController.StepCallback stepDetectorCallback=new StepController.StepCallback() {
//        @Override
//        public void catchStep(int step) {
//            navigation.refreshStep(stepController.getLength(),stepController.getState());
//        }
//    };

    //加速度控制器
    private AccController accController;
    private AccController.AccCallback accCallback=new AccController.AccCallback() {
        @Override
        public void BufferDetector(float speed) {
            navigation.refreshBufferSpeed(speed);
        }
    };

    //陀螺仪控制器
    private GyroscopeController gyroscopeController;
    private GyroscopeController.GyroscopeControllerCallback gyroscopeControllerCallback=new GyroscopeController.GyroscopeControllerCallback() {
        @Override
        public void refreshGyr(float angle,float speed) {
            int a=(int)Math.toDegrees(angle);
            a%=360;
            navigation.refreshGyroscopeAngle(-angle,speed);
            map.setAngle((float) Math.toDegrees(-angle));
        }

        @Override
        public void turnDetecte(int state) {

        }
    };

    //磁场控制器
    private MagController magController;
    private MagController.MagDetectorCallback magDetectorCallback=new MagController.MagDetectorCallback() {
        @Override
        public void MagState(float var, final float speed) {
            navigation.refreshMagSpeed(speed);
        }
    };

    //导航
    private Navigation navigation;
    private Navigation.NavigationCallback navigationCallback=new Navigation.NavigationCallback() {
        @Override
        public void Position(final int x, final int y) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (map!=null){
                        map.addText("  speed:"+navigation.mixSpeed*3.6);
                        map.setPosition(x,y);
                        map.invalidate();
                    }
                }
            });

        }

        @Override
        public void Orientation(float angle) {
            if (map!=null) {
                map.setAngle(angle);
            }
        }
    };

    //相机
    private MyCamera1 camera1;
    private MyCamera1.Camera1Callback camera1Callback=new MyCamera1.Camera1Callback() {
        @Override
        public void UpdateText(String text) {

        }

        @Override
        public void UpdateImage(Bitmap bitmap) {
            if (iv!=null){
                iv.setImageBitmap(bitmap);
            }
        }

        @Override
        public int UpdateLamp(int LampId) {
            map.setText(LampId+"");
            return navigation.refreshLamp(LampId);
        }

        @Override
        public int[] getMostClosePoint() {
            return Utils.getP(navigation.Mx,navigation.My);
        }
    };

    //拍照线程
    private Thread cameraThread;

    //UI
    private TextView showMessage;
    private Map map;
    private Button bt,camerabt;
    private SurfaceView sv;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

//        List<Lamp> lamps=Utils.getLamp();
//
//        int[][] topo=new int[8][8];
//        for (int i=0;i<lamps.size();i++){
//            for(int j=i+1;j<lamps.size();j++){
//                topo[i][j]=Math.abs(lamps.get(i).x-lamps.get(j).x)+
//                        Math.abs(lamps.get(i).y-lamps.get(j).y);
//                topo[j][i]=topo[i][j];
//            }
//        }
//        for(int i=0;i<topo.length;i++){
//            String s="";
//            for (int j=0;j<topo.length;j++){
//                s+=topo[i][j]+",";
//            }
//            Log.e("kitty",s);
//        }

        Init();
    }

    private void Init(){
        //申请权限
        if(!hasPermissionsGranted(PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(PERMISSIONS, 1);
            }
        }

        //申请传感器
        sensorController=new SensorController((SensorManager) getSystemService(Context.SENSOR_SERVICE),sensorCallback);
        //sensorController.registerSensor(Sensor.TYPE_ACCELEROMETER,SensorManager.SENSOR_DELAY_GAME);
        sensorController.registerSensor(Sensor.TYPE_MAGNETIC_FIELD,SensorManager.SENSOR_DELAY_GAME);
        //sensorController.registerSensor(Sensor.TYPE_ORIENTATION,SensorManager.SENSOR_DELAY_GAME);
        sensorController.registerSensor(Sensor.TYPE_GYROSCOPE,SensorManager.SENSOR_DELAY_FASTEST);

        //计步控制器
        //stepController=new StepController(stepDetectorCallback);

        //加速度控制器
        //accController=new AccController(accCallback);

        //陀螺仪控制器
        gyroscopeController=new GyroscopeController(gyroscopeControllerCallback);

        //磁场控制器
        magController=new MagController(magDetectorCallback);

        //导航
        navigation=new Navigation(navigationCallback);

        //UI
        showMessage=findViewById(R.id.show_text);
        
        map=findViewById(R.id.map);
        map.setLine(Utils.getLine());
        map.setLamp(Utils.getLamp());
        map.setText("迦南地");

        bt=findViewById(R.id.orinbutton);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigation.InitPosition(0,0);
                gyroscopeController.correctAngle(0);
            }
        });

//        camerabt=findViewById(R.id.cbutton);
//        camerabt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (camera1==null) {
//                    Toast.makeText(MainActivity.this,"相机在初始化",Toast.LENGTH_SHORT).show();
//                } else {
//                    if (cameraThread == null) {
//                        cameraThread = new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try{
//                                    Thread.sleep(500);
//                                    while (true){
//                                        camera1.TakePhoto();
//                                        Thread.sleep(100);
//                                    }
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                        cameraThread.start();
//                        Toast.makeText(MainActivity.this,"开始连续拍照",Toast.LENGTH_SHORT)
//                                .show();
//                    }else{
//                        cameraThread.interrupt();
//                        cameraThread=null;
//                        Toast.makeText(MainActivity.this,"停止拍照",Toast.LENGTH_SHORT)
//                                .show();
//                    }
//                }
//            }
//        });

        sv=findViewById(R.id.surfaceView);

        //相机初始化
        new Thread(new Runnable() {
            @Override
            public void run() {
                camera1 = new MyCamera1(MainActivity.this, sv, camera1Callback, 0);
                camera1.openCamera();
            }
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //关闭标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //关闭状态栏
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(flag);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length == PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this,"请给权限",Toast.LENGTH_SHORT);
                        return;
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT>21) {
                    requestPermissions(permissions, requestCode);
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT>=23&&this.checkSelfPermission(permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
