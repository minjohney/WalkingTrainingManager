package com.example.user.walkingapp;

import android.Manifest;
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
    protected Sensor stepCountSensor;
    protected TextView tvStepCount;
    protected Button MyLocation;
    private int stepCounter;
    protected MyLocationListener myLocationListener;
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

        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new MyLocationListener();
        long minTime = 1000;
        float minDistance = 0;
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


        MyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLocation();
            }
        });

        tvStepCount = (TextView) findViewById(R.id.tvStepCount);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if (stepCountSensor == null) {
            Toast.makeText(this, "No Step Detect Sensor", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLocation() {
        latitude = myLocationListener.latitude;
        longitude = myLocationListener.longitude;
        altitude = myLocationListener.altitude;
        tvLocation.setText("\n위도:" + longitude + "\n경도:" + latitude + "\n고도:" + altitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + latitude + "," + longitude + "?z=15"));
        startActivity(intent);
    }


    private void show() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
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

    public void hide() {
        NotificationManagerCompat.from(this).cancel(1);
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, stepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);

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
                if (stepCounter < Size) {
                    show();
                } else if (stepCounter >= Size) {
                    hide();
                    show2();
                }
            }
        }
    }


    private void show2() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
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
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
