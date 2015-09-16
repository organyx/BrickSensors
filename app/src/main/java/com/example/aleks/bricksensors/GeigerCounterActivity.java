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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class GeigerCounterActivity extends AppCompatActivity implements SensorEventListener,SoundPool.OnLoadCompleteListener {

    private TextView tvRadians;
    private TextView tvDegrees;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float[] readingAccelerometer = new float[3];

    private SoundPool soundPool;
    private int idHighClick;
    private int idMidClick;
    private int idLowClick;
    private int highClick;
    private int midClick;
    private int lowClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geiger_counter);

        tvRadians = (TextView) findViewById(R.id.tvRadians);
        tvDegrees = (TextView) findViewById(R.id.tvDegrees);

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

        highClick = soundPool.load(this, R.raw.geiger_high,1);
        midClick = soundPool.load(this, R.raw.geiger_mid,2);
        lowClick = soundPool.load(this, R.raw.geiger_low,3);
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
        getMenuInflater().inflate(R.menu.menu_geiger_counter, menu);
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
    public void onSensorChanged(SensorEvent event) {
        float[] rotation = new float[9];
        float[] orientation = new float[3];

        if(event.sensor == accelerometer)
        {
            readingAccelerometer[0] = event.values[0];
            readingAccelerometer[1] = event.values[1];
            readingAccelerometer[2] = event.values[2];
        }

        SensorManager.getOrientation(rotation, orientation);
        float azimuthRadians = orientation[0];
        float azimuthDegrees = -(float) (Math.toDegrees(azimuthRadians) + 360) % 360;



        doGeigerStuff(azimuthRadians, azimuthDegrees);
//        currentCompassAngle = azimuthDegrees;
    }

    public void doGeigerStuff(float a, float b)
    {
        tvDegrees.setText("Degrees: " + a);
        tvRadians.setText("Radians: " + b);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume()
    {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        soundPool.autoResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
        soundPool.autoPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        soundPool.stop(idHighClick);
        soundPool.stop(idMidClick);
        soundPool.stop(idLowClick);
        soundPool.release();
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if(status == 0){
            if (sampleId == highClick)
            {
                idHighClick = soundPool.play(highClick, 0.2f, 0.2f, 1, -1, 1f);
            }
            if (sampleId == midClick)
            {
                idMidClick = soundPool.play(midClick, 0.2f, 0.2f, 1, -1, 1f);
            }
            if (sampleId == lowClick)
            {
                idLowClick = soundPool.play(lowClick, 0.2f, 0.2f, 1, -1, 1f);
            }
        }
        else
        {
            Toast.makeText(GeigerCounterActivity.this, "Error loading sound: " + sampleId, Toast.LENGTH_LONG).show();
        }
    }
}
