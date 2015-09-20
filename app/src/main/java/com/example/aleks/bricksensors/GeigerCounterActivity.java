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

    private float rads = 0;

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

        Typeface customFont = Typeface.createFromAsset(this.getAssets(), "fonts/stalker_font.ttf");

        tvA.setTypeface(customFont);
        tvB.setTypeface(customFont);
        tvC.setTypeface(customFont);
        tvRads.setTypeface(customFont);

        ivHumanStatus = (ImageView)findViewById(R.id.ivHumanStatus);

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
            readingAccelerometer = lowPass(event.values, readingAccelerometer);
        }

//        SensorManager.getOrientation(rotation, orientation);
//        float azimuthRadians = orientation[0];
//        float azimuthDegrees = -(float) (Math.toDegrees(azimuthRadians) + 360) % 360;



        doGeigerStuff(readingAccelerometer);
//        currentCompassAngle = azimuthDegrees;
    }

    public void doGeigerStuff(float[] readings)
    {
//        Drawable skeleton = getDrawable(R.drawable.skeleton);
        float x = readings[0];
        float y = readings[1];
        float z = readings[2];

        tvA.setText("X: " + x);
        tvB.setText("Y: " + y);
        tvC.setText("Z: " + z);

        if(loadingCompleted) {

            if (y >= 9f &&  y <= 10f)
            {
                ivHumanStatus.setImageResource(R.drawable.doing_good);
                soundPool.play(lowClick, 0.5f, 0.5f, 5, -1, 1f);
                tvRads.setText("Total Rads: " + rads);
            }
            else if(y >= 7f && y < 9f)
            {

                ivHumanStatus.setImageResource(R.drawable.protivogaz);
                tvRads.setText("Total Rads: " + (rads += 0.05));
            }
            else if (y >= 3f && y < 7f)
            {
                soundPool.play(midClick, 0.5f, 0.5f, 5, -1, 1f);
                ivHumanStatus.setImageResource(R.drawable.zombie);
                tvRads.setText("Total Rads: " + (rads += 0.1));
            }
            else if (y >= 0f && y < 3f )
            {
                soundPool.play(highClick, 0.5f, 0.5f, 5, -1, 1f);
                ivHumanStatus.setImageResource(R.drawable.skeleton);
                tvRads.setText("Total Rads: " + (rads += 0.25));
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
                Log.d("High", "Loaded High");
            }
            if (sampleId == midClick)
            {
                Log.d("Mid", "Loaded Mid");
            }
            if (sampleId == lowClick)
            {
                Log.d("Low", "Loaded Low");
            }
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
