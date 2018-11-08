package com.example.yushichao.parkingdemo2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
            stepController.refreshAcceleration(accs);
        }

        @Override
        public void refreshMag(float[] mags) {

        }

        @Override
        public void refreshOri(float[] angles) {

        }

        @Override
        public void refreshGyr(float[] values) {

        }
    };

    //计步控制器
    private StepController stepController;
    private StepController.StepCallback stepDetectorCallback=new StepController.StepCallback() {
        @Override
        public void catchStep(final int step) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showMessage.setText(step+"");
                }
            });
        }
    };

    //加速度控制器
    private AccController accController;
    private AccController.AccCallback accCallback=new AccController.AccCallback() {
        @Override
        public void BufferDetector(float speed) {

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
                        map.setPosition(x,y);
                        showMessage.setText(x+","+y);
                    }
                }
            });

        }
    };

    //UI
    private TextView showMessage;
    private Map map;
    private Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
        sensorController.registerSensor(Sensor.TYPE_ACCELEROMETER,SensorManager.SENSOR_DELAY_GAME);
        sensorController.registerSensor(Sensor.TYPE_MAGNETIC_FIELD,SensorManager.SENSOR_DELAY_GAME);
        sensorController.registerSensor(Sensor.TYPE_ORIENTATION,SensorManager.SENSOR_DELAY_GAME);
        sensorController.registerSensor(Sensor.TYPE_GYROSCOPE,SensorManager.SENSOR_DELAY_GAME);

        //计步控制器
        stepController=new StepController(stepDetectorCallback);

        //加速度控制器
        accController=new AccController(accCallback);

        //导航
        navigation=new Navigation(navigationCallback);

        //UI
        showMessage=(TextView)findViewById(R.id.show_text);
        
        map=(Map)findViewById(R.id.map);
        map.setLine(Utils.getLine());
        map.setLamp(Utils.getLamp());
        map.setText("迦南地");

        bt=(Button)findViewById(R.id.orinbutton);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigation.InitPosition(0,0);
            }
        });
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
