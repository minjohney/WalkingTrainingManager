package com.example.user.walkingapp;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    protected SensorManager sensorManager;
    protected Sensor MyStepCount;
    protected TextView tvStepCount;
    protected Button MyLocation;
    private int stepCounter;
    protected MyLocationListener myLocationListener;
    protected LocationManager locationManager;
    protected double latitude;
    protected double longitude;
    protected double altitude;
    private int Size = 100;
    protected TextView tvLocation;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLocation = (TextView) findViewById(R.id.text2);
        MyLocation = (Button) findViewById(R.id.myLocation);

        //위치 서비스를 사용하겠습니다.
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new MyLocationListener();
        long minTime = 1000;
        float minDistance = 0; //미세한 위치 변화도 감지
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, myLocationListener);


        //내가 어디 걷고 있을까 라는 버튼을 눌렀을 때 함수 불러옴
        MyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLocation();
            }
        });

        tvStepCount = (TextView) findViewById(R.id.tvStepCount);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        MyStepCount = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR); //걸음 측정을 위한 센서 불러옴

        //센서가 없는 경우 Toast 메시지를 띄운다.
        if (MyStepCount == null) {
            Toast.makeText(this, "No Step Detect Sensor", Toast.LENGTH_SHORT).show();
        }
    }

    //나의 현재 위치를 보여주는 함수(->지도에 위치 표시)
    private void showLocation() {
        latitude = myLocationListener.latitude;
        longitude = myLocationListener.longitude;
        altitude = myLocationListener.altitude;
        tvLocation.setText("\n위도:" + longitude + "\n경도:" + latitude + "\n고도:" + altitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + latitude + "," + longitude + "?z=15"));
        startActivity(intent);
    }


    //최대 백걸음 미만일 때 상단에 뜨는 알림 생성
    private void TryAgain() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.drawable.walk);
        builder.setContentTitle("서민진 트레이너입니다");
        builder.setContentText("조금만 힘내서 걸어봐요");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.walk);
        builder.setLargeIcon(largeIcon);

        builder.setColor(Color.GREEN);

        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(ringtoneUri);

        long[] vibrate = {0, 100, 200, 300};
        builder.setVibrate(vibrate);
        builder.setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }
        manager.notify(1, builder.build());
    }

    //100걸음 이상이 되었을 때 TryAgain() 함수를 닫는것
    public void hide() {
        NotificationManagerCompat.from(this).cancel(1);
    }

    //최대로 지정한 100걸음이 되었을 때 생성되는 푸시 알림
    private void Goal() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.drawable.walk);
        builder.setContentTitle("서민진 트레이너입니다");
        builder.setContentText("백걸음 달성하셨어요 건강함의 일인자가 되셨어요!");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.walk);
        builder.setLargeIcon(largeIcon);

        builder.setColor(Color.GREEN);

        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(ringtoneUri);

        long[] vibrate = {0, 100, 200, 300};
        builder.setVibrate(vibrate);
        builder.setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }
        manager.notify(2, builder.build());
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, MyStepCount, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (sensorEvent.values[0] == 1.0f) {
                stepCounter++;
                tvStepCount.setText(String.valueOf(stepCounter) + "걸음");
                if (stepCounter < Size) { //100걸음 미만일때
                    TryAgain();
                } else if (stepCounter == Size) { //100걸음 이상일때
                    hide();
                    Goal();
                }
            }
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}