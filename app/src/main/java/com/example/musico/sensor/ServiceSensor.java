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
    long lastUpdate = 0;
    float[] gravity =  new float[3];
    File file = null;
    File arq = null;
    String data;
    StringBuilder vals = new StringBuilder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {//executa quando o service for chamado
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//objeto que vai escutar o acelerometro
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);//seta intervalo de leitura
        new Thread(new Runnable() {
            public void run(){//thread para gravar em arquivo
                while(true){//durante toda a execução da aplicação
                    try {
                        Thread.sleep(10000);//em intervalos de 10 segundos
                        if(vals.length() != 0){
                            gravarArquivo();//método para gravar em arquivo
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();//inicia thread
        return super.onStartCommand(intent, flags, startId);
    }

    public void gravarArquivo() {
        String state = Environment.getExternalStorageState();//recebe o path do diretório
        if (Environment.MEDIA_MOUNTED.equals(state)) {//se estiver ok o armazenamento
            try {
                data = new SimpleDateFormat("dd-MMM-yyyy").format(new Date(System.currentTimeMillis()));//recebe data atual
                file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "Sensor");//cria objeto para manipular arquivo no diretorio Sensor
                file.mkdirs();//se não existe o diretório, cria-o
                arq = new File(file, data+"Eixos.txt");//cria o arquivo com a data e nome Eixos caso não exista
                FileWriter fw = new FileWriter(arq, true);//abre o arquivo para escrita
                fw.write(vals.toString());//escreve nele os valores apendicionados lidos dos sensores
                vals.setLength(0);//depois de gravar zera a variável
                fw.flush();//limpa o objeto de escrita
                fw.close();//fecha o objeto
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getAbsolutePath()}, null, null);//exibe o arquivo imediatamente ao alterá-lo
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{arq.getAbsolutePath()}, null, null);//exibe o diretório imediatamente após cria-lo
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
    public void onSensorChanged(SensorEvent event) {//metodo que escuta o sensor quando este detectar mudança
        Sensor mySensor = event.sensor;//objeto que recebe dados do sensor
        long curTime = System.currentTimeMillis();//obtem o tempo atual em milisegundos
        if ((curTime - lastUpdate) > 20) {//a cada 20 milisegundos
            lastUpdate = curTime;
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {//verifica o acelerômetro
                final float ALPHA = (float) 0.8;//anula gravidade
                gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * event.values[0];
                gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * event.values[1];
                gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * event.values[2];

                x = event.values[0] - gravity[0];
                y = event.values[1] - gravity[1];
                z = event.values[2] - gravity[2];
                vals.append(df.format(x)).append(";").append(df.format(y)).append(";").append(df.format(z)).append("\r\n");//quebra de linha para o Windows
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
