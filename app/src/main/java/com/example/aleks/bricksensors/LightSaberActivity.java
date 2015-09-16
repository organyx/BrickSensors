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

public class LightSaberActivity extends AppCompatActivity implements SoundPool.OnLoadCompleteListener, SensorEventListener {

    private ImageView lightSaber;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float[] readingAccelerometer = new float[3];

    private float ALPHA = 0.15f;

    private SoundPool soundPool;
    private int slowSaber;
    private int swing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_saber);

        lightSaber = (ImageView)findViewById(R.id.ivLightSaber);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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

        slowSaber = soundPool.load(this, R.raw.slow_sabr, 1);
        swing = soundPool.load(this, R.raw.sthswng1, 2);
    }

    @SuppressWarnings("deprecation")
    private void createOldSoundPool() {
        soundPool = new SoundPool(50, AudioManager.STREAM_MUSIC, 0);
    }

    @TargetApi(21)
    private void createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
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
        if (status == 0)
        {
            if(sampleId == slowSaber)
            {
               this.soundPool.play(slowSaber, 0.2f, 0.2f, 1, -1, 1f);
            }
        }
        else
        {
            Toast.makeText(LightSaberActivity.this, "Error loading sound: "+sampleId, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        soundPool.stop(slowSaber);
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
        if(event.sensor == accelerometer)
        {
//            readingAccelerometer[0] = event.values[0];
//            readingAccelerometer[1] = event.values[1];
//            readingAccelerometer[2] = event.values[2];
            readingAccelerometer = lowPass(event.values.clone(), readingAccelerometer);
        }
//        soundPool.setVolume(slowSaber, 0.8f, 0.8f);
//        Log.d("Accel", "Accelerometer Data: " + readingAccelerometer[0]);
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
