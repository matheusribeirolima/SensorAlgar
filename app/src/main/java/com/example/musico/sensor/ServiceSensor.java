package com.example.musico.sensor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServiceSensor extends Service implements SensorEventListener {

    SensorManager senSensorManager;
    Sensor senAccelerometer;
    float x, y, z;
    DecimalFormat df =  new DecimalFormat("0.000");
    //int contador = 0;
    long lastUpdate = 0;
    //float last_x, last_y, last_z;
    //static final int SHAKE_THRESHOLD = 400;
    float[] gravity =  new float[3];
    File file = null;
    File arq = null;
    String data, hora;
    StringBuilder vals = new StringBuilder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        new Thread(new Runnable() {
            public void run(){
                while(true){
                    try {
                        Thread.sleep(10000);
                        hora = new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()));
                        if(vals.length() != 0){
                            gravarArquivo();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    public void gravarArquivo() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            try {
                data = new SimpleDateFormat("dd-MMM-yyyy").format(new Date(System.currentTimeMillis()));
                file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "Sensor");
                file.mkdirs();
                arq = new File(file, data+"Eixos.txt");
                FileWriter fw = new FileWriter(arq, true);
                fw.write(hora + "\n" + vals + "\n");
                vals.setLength(0);
                //contador = 0;
                fw.flush();
                fw.close();
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getAbsolutePath()}, null, null);
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{arq.getAbsolutePath()}, null, null);
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        long curTime = System.currentTimeMillis();
        if ((curTime - lastUpdate) > 20) {
            //long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                final float ALPHA = (float) 0.8;
                gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * event.values[0];
                gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * event.values[1];
                gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * event.values[2];

                x = event.values[0] - gravity[0];
                y = event.values[1] - gravity[1];
                z = event.values[2] - gravity[2];
                vals.append(df.format(x)).append(";").append(df.format(y)).append(";").append(df.format(z)).append("\n");
                //contador += 1;
                /*
                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
                if (speed > SHAKE_THRESHOLD) {
                    Toast.makeText(this, "Mexeu", Toast.LENGTH_SHORT).show();
                }
                last_x = x;
                last_y = y;
                last_z = z;
                */
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
