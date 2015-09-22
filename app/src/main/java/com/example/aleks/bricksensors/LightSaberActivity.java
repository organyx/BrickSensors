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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LightSaberActivity extends AppCompatActivity implements SoundPool.OnLoadCompleteListener, SensorEventListener {

    private ImageView lightSaber;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnometer;
    private float[] readingAccelerometer = new float[3];
    private float[] readingMagnometer = new float[3];

    private SoundPool soundPool;
    private int saberSwing;
    private int saberOn;
    private int saberHum;
    private int imperialMarch;

//    private boolean marchPlaying = false;

//    private TextView tvLsA;
//    private TextView tvLsB;
//    private TextView tvLsC;

    private boolean s1 = false;
    private boolean s2 = false;
    private boolean s3 = false;
    private boolean s4 = false;
    private boolean loadingCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_saber);

        lightSaber = (ImageView)findViewById(R.id.ivLightSaber);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

//        tvLsA = (TextView) findViewById(R.id.tvLsA);
//        tvLsB = (TextView) findViewById(R.id.tvLsB);
//        tvLsC = (TextView) findViewById(R.id.tvLsC);

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
        saberOn = soundPool.load(this, R.raw.saber_on, 1);
        saberHum = soundPool.load(this, R.raw.saber_hum2, 1);
        saberSwing = soundPool.load(this, R.raw.saber_swing2, 1);
        imperialMarch = soundPool.load(this, R.raw.imperial_march, 1);
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
            if(sampleId == saberOn)
            {
                this.soundPool.play(saberOn, 1f, 1f, 1, 0, 1f);
                s1 = true;
                Log.d("onLoadComplete", "Loaded saberOn");
            }
            if(sampleId == saberHum)
            {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.soundPool.play(saberHum, 0.7f, 0.7f, 1, -1, 1f);
                s2 = true;
                Log.d("onLoadComplete", "Loaded saberHum");
            }
            if(sampleId == saberSwing)
            {
                s3 = true;
                Log.d("onLoadComplete", "Loaded Swing");
            }
            if(sampleId == imperialMarch)
            {
                s4 = true;
                Log.d("onLoadComplete", "Loaded Swing");
            }
            if(s1 && s2 && s3 && s4)
            {
                loadingCompleted = true;
            }
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
        soundPool.stop(saberOn);
        soundPool.stop(saberHum);
        soundPool.stop(imperialMarch);
        soundPool.release();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnometer, SensorManager.SENSOR_DELAY_GAME);
        soundPool.autoResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnometer);
        soundPool.autoPause();
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

        if(event.sensor == magnometer)
        {
            readingMagnometer[0] = event.values[0];
            readingMagnometer[1] = event.values[1];
            readingMagnometer[2] = event.values[2];
        }

        SensorManager.getRotationMatrix(rotation, null, readingAccelerometer, readingMagnometer);
        SensorManager.getOrientation(rotation, orientation);
        Log.d("onSensorChanged", "Orientation: " + orientation[0] + ", " + orientation[1] + ", " + orientation[2] );

        float azimuthRadians = orientation[0];
        float azimuthDegrees = -(float) (Math.toDegrees(azimuthRadians) + 360) % 360;


            //doSaberSounds(readingAccelerometer);
        doSaberSoundsNew(orientation);
        }

    public void doSaberSoundsNew(float[] orientation)
    {
        float pitch = orientation[1]*= 57.2957795f;
        float roll = orientation[2]*= 57.2957795f;

//        if (orientation[0]<0)
//            orientation[0] += 360;
//        // Pitch scaling
//        if (orientation[1]<-90)
//            orientation[1] += (-2*(90+orientation[1]));
//        else if (orientation[1]>90)
//            orientation[1] += (2*(90-orientation[1]));
//          DEBUG CODE
//        tvLsA.setText("X: " + orientation[0]);
//        tvLsB.setText("Y: " + pitch);
//        tvLsC.setText("Z: " + roll);

        if(loadingCompleted)
        {
//            if (pitch < -45 && pitch > -135) {
//                soundPool.play(saberSwing, 0.8f, 0.8f, 1, 0, 1f);
//            } else if (pitch > 45 && pitch < 135) {
//                soundPool.play(saberSwing, 0.8f, 0.8f, 1, 0, 1f);
//            }

            //soundPool.play(saberHum, 1f, 1f, 1, -1, 1f);
//
            if (roll > 45 && roll < 110) {
                soundPool.play(saberSwing, 0.8f, 0.8f, 1, 0, 1f);
            } else if (roll < -45 && roll > -110) {
                soundPool.play(saberSwing, 0.8f, 0.8f, 1, 0, 1f);
            }
        }
    }

    // USING ACCELEROMETER ONLY
//    public void doSaberSounds(float[] readings)
//    {
//        if(loadingCompleted) {
////           if(!swingPlayedOnce)
////           {
//               if (readings[0] < 4 && readings[0] > 3.7)
//               {
//                   // soundPool.pause(saberHum);
//                   soundPool.play(saberSwing, 0.8f, 0.8f, 1, 0, 1f);
//                   try {
//                       Thread.sleep(400);
//                   } catch (InterruptedException e) {
//                       e.printStackTrace();
//                   }
//                   //soundPool.stop(saberSwing);
////                  soundPool.resume(saberHum);
//                   soundPool.play(saberHum, 0.7f, 0.7f, 1, -1, 1f);
////                   swingPlayedOnce = true;
//               }
//               else if (readings[0] < -3.7 && readings[0] > -4)
//               {
//                   //soundPool.pause(saberHum);
//                   soundPool.play(saberSwing, 0.8f, 0.8f, 1, 0, 1f);
//                   try {
//                       Thread.sleep(400);
//                   } catch (InterruptedException e) {
//                       e.printStackTrace();
//                   }
//                   soundPool.play(saberHum, 0.7f, 0.7f, 1, -1, 1f);
////                soundPool.resume(saberHum);
////                   swingPlayedOnce = true;
//               }
////           }
//
//        }
//    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
//    SOUNDPOOL IS NOT VERY GOOD, CANNOT DETECT WHEN SOUNDS STOP PLAYING, THUS METHOD NOT USED
//    public void onBtnVaderClick(View view) {
//        Log.d("Imperial", "marchPlaying: " + marchPlaying);
//        if (!marchPlaying)
//        {
//            marchPlaying = true;
//            soundPool.play(imperialMarch, 1f, 1f, 1, 0, 1f);
//            marchPlaying = false;
//            Log.d("Imperial", "marchPlaying: " + marchPlaying);
//        }
//        else if (marchPlaying)
//        {
//            soundPool.stop(imperialMarch);
//            marchPlaying = false;
//            Log.d("Imperial", "marchPlaying: " + marchPlaying);
//        }
//    }
}
