package com.example.aleks.bricksensors;

import android.annotation.TargetApi;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;
import android.widget.Toast;

public class GeigerCounterActivity extends AppCompatActivity implements SensorEventListener,SoundPool.OnLoadCompleteListener {

    private TextView tvA;
    private TextView tvB;
    private TextView tvC;
    private TextView tvRads;
    private ImageView ivHumanStatus;

    private float totalRads;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnometer;
    private float[] readingAccelerometer = new float[3];
    private float[] readingMagnometer = new float[3];

    private SoundPool soundPool;
    private boolean s1 = false;
    private boolean s2 = false;
    private boolean s3 = false;
    private int highClick;
    private int midClick;
    private int lowClick;

    private float ALPHA = 0.15f;

    private boolean loadingCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geiger_counter);

        tvA = (TextView) findViewById(R.id.tvA);
        tvB = (TextView) findViewById(R.id.tvB);
        tvC = (TextView) findViewById(R.id.tvC);
        tvRads = (TextView) findViewById(R.id.tvRads);

        totalRads = 0;

        Typeface customFont = Typeface.createFromAsset(this.getAssets(), "fonts/stalker_font.ttf");

        tvA.setTypeface(customFont);
        tvB.setTypeface(customFont);
        tvC.setTypeface(customFont);
        tvRads.setTypeface(customFont);

        ivHumanStatus = (ImageView)findViewById(R.id.ivHumanStatus);

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
        highClick = soundPool.load(this, R.raw.geiger_high, 1);
        midClick = soundPool.load(this, R.raw.geiger_mid, 1);
        lowClick = soundPool.load(this, R.raw.geiger_low, 1);
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
        //Log.d("onSensorChanged", "onSensorChanged HAS STARTED");
        float[] rotation = new float[9];
        float[] orientation = new float[3];

        if(event.sensor == accelerometer)
        {
            readingAccelerometer[0] = event.values[0];
            readingAccelerometer[1] = event.values[1];
            readingAccelerometer[2] = event.values[2];
//            readingAccelerometer = lowPass(event.values, readingAccelerometer);
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


        doGeigerStuff(readingAccelerometer);
//        currentCompassAngle = azimuthDegrees;
    }

    public void doGeigerStuff(float[] readings)
    {
        float x = readings[0];
        float y = readings[1];
        float z = readings[2];

        tvA.setText("X: " + x);
        tvB.setText("Y: " + y);
        tvC.setText("Z: " + z);
        tvRads.setText("Total Rads: " + totalRads);

        if(loadingCompleted) {

            if ((y >= 9f &&  y <= 10f))
            {
                ivHumanStatus.setImageResource(R.drawable.doing_good);
                soundPool.play(lowClick, 0.5f, 0.5f, 5, -1, 1f);
            }
            else if((y >= 6f && y < 9f))
            {

                ivHumanStatus.setImageResource(R.drawable.protivogaz);
                tvRads.setText("Total Rads: " + (totalRads += 0.05));
            }
            else if (y >= 3f && y < 6f)
            {
                soundPool.play(midClick, 0.5f, 0.5f, 5, -1, 1f);
                ivHumanStatus.setImageResource(R.drawable.zombie);
                tvRads.setText("Total Rads: " + (totalRads += 0.25));
            }
            else if ((y >= 0f && y < 3f ))
            {
                soundPool.play(highClick, 0.5f, 0.5f, 5, -1, 1f);
                ivHumanStatus.setImageResource(R.drawable.skeleton);
                tvRads.setText("Total Rads: " + (totalRads += 0.5));
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume()
    {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnometer, SensorManager.SENSOR_DELAY_GAME);
        soundPool.autoResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnometer);
        soundPool.autoPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        soundPool.stop(highClick);
        soundPool.stop(midClick);
        soundPool.stop(lowClick);
        soundPool.release();
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        Log.d("onLoadComplete", "onLoadComplete HAS STARTED");

        if(status == 0){
            if (sampleId == highClick)
            {
                s1 = true;
                Log.d("High", "Loaded High");
            }
            if (sampleId == midClick)
            {
                s2 = true;
                Log.d("Mid", "Loaded Mid");
            }
            if (sampleId == lowClick)
            {
                s3 = true;
                Log.d("Low", "Loaded Low");
            }
            if(s1 && s2 && s3)
                loadingCompleted = true;
        }
        else
        {
            Toast.makeText(GeigerCounterActivity.this, "Error loading sound: " + sampleId, Toast.LENGTH_LONG).show();
        }
        Log.d("onLoadComplete", "onLoadComplete HAS FINISHED");
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
