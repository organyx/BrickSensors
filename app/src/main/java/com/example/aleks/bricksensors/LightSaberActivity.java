package com.example.aleks.bricksensors;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class LightSaberActivity extends AppCompatActivity implements SoundPool.OnLoadCompleteListener, SensorEventListener {

    private ImageView lightSaber;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnometer;
    private float[] readingAccelerometer = new float[3];
    private float[] readingMagnometer = new float[3];

    private float ALPHA = 0.15f;

    private SoundPool soundPool;
    private int saberSwing;
    private int hf;
    private int lf;

    private boolean loadingCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_saber);

        lightSaber = (ImageView)findViewById(R.id.ivLightSaber);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            createNewSoundPool();
            Toast.makeText(this, "Lollipop or newer", Toast.LENGTH_LONG).show();
        }
        else
        {
            createOldSoundPool();
            Toast.makeText(this, "Pre Lollipop", Toast.LENGTH_LONG).show();
        }
        soundPool.setOnLoadCompleteListener(this);
        hf = soundPool.load(this, R.raw.hf, 1);
        lf = soundPool.load(this, R.raw.lf, 2);
        saberSwing = soundPool.load(this, R.raw.saberswing, 3);
    }

    @SuppressWarnings("deprecation")
    private void createOldSoundPool() {
        soundPool = new SoundPool(50, AudioManager.STREAM_MUSIC, 0);
    }

    @TargetApi(21)
    private void createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_light_saber, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
    {
        Log.d("onLoadComplete", "onLoadComplete HAS STARTED");
        if (status == 0)
        {
            if(sampleId == hf)
            {
                soundPool.play(hf, 0.2f, 0.2f, 1, -1, 1f);
                Log.d("hf", "Loaded hf");
            }
            if(sampleId == lf)
            {
                soundPool.play(lf, 1f, 1f, 1, -1, 1f);
                Log.d("lf", "Loaded lf");
            }
            if(sampleId == saberSwing)
            {
                Log.d("Swing", "Loaded Swing");
            }
            loadingCompleted = true;
        }
        else
        {
            Toast.makeText(LightSaberActivity.this, "Error loading sound: "+sampleId, Toast.LENGTH_LONG).show();
        }
        Log.d("onLoadComplete", "onLoadComplete HAS FINISHED");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        soundPool.stop(saberSwing);
        soundPool.stop(hf);
        soundPool.stop(lf);
        soundPool.release();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        soundPool.autoResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
        soundPool.autoPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float[] rotation = new float[9];
        float[] orientation = new float[3];

        if(event.sensor == accelerometer)
        {
//            readingAccelerometer[0] = event.values[0];
//            readingAccelerometer[1] = event.values[1];
//            readingAccelerometer[2] = event.values[2];
            readingAccelerometer = lowPass(event.values, readingAccelerometer);
        }

        if(event.sensor == magnometer)
        {
//            readingMagnometer[0] = event.values[0];
//            readingMagnometer[1] = event.values[1];
//            readingMagnometer[2] = event.values[2];
            readingMagnometer = lowPass(event.values, readingMagnometer);
        }

        SensorManager.getRotationMatrix(rotation, null, readingAccelerometer, readingMagnometer);
        SensorManager.getOrientation(rotation, orientation);
        float azimuthRadians = orientation[0];
        float azimuthDegrees = -(float) (Math.toDegrees(azimuthRadians) + 360) % 360;

        doSaberSounds(readingAccelerometer, azimuthDegrees);
    }

    public void doSaberSounds(float[] readings, float deg)
    {
        if(loadingCompleted) {
            if (readings[0] < 8)
//                Log.d("d", "d");
                soundPool.play(saberSwing, 0.8f, 0.8f, 1, 1, 1f);
//            if (readings[0] < -4 && readings[0] > -6 || readings[0] > 4 && readings[0] <6) {
//                soundPool.play(saberSwing, 0.2f, 0.2f, 1, 1, 1f);
//                //soundPool.setVolume(saberSwing, 0.1f, 0.1f);
//            }
//            if(readings[0] <= -6 || readings[0] >= 6)
//            {
////                soundPool.play(saberSwing, 0.8f, 0.8f, 1, 1, 1f);
////                try {
////                    TimeUnit.MILLISECONDS.sleep(500);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//                soundPool.setVolume(saberSwing, 0.8f, 0.8f);
//            }
//            if(readings[0] <= -8 || readings[0] >= 8)
//            {
////                soundPool.play(saberSwing, 0.2f, 0.2f, 1, 1, 1f);
////                try {
////                    TimeUnit.MILLISECONDS.sleep(500);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//                soundPool.setVolume(saberSwing, 0.1f, 0.1f);
//            }
            //===============================================
//            if(Math.abs(readings[0]) > Math.abs(readings[1]))
//            {
//                if (readings[0] < 0)
//                {
//                    soundPool.play(saberSwing, 0.2f, 0.2f, 1, 1, 1f);
//                }
//                if (readings[0] > 0)
//                {
//                    soundPool.play(saberSwing, 0.2f, 0.2f, 1, 1, 1f);
//                }
//            }
//            else
//            {
//                if (readings[1] < 0)
//                {
//                    soundPool.play(saberSwing, 0.2f, 0.2f, 1, 1, 1f);
//                }
//                if (readings[1] > 0)
//                {
//                    soundPool.play(saberSwing, 0.2f, 0.2f, 1, 1, 1f);
//                }
//            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected float[] lowPass(float[] input, float[] output)
    {
        if(output == null)
            return input;
        for (int i = 0; i < input.length; i++)
        {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
