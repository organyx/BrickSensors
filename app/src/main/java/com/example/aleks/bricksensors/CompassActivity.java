package com.example.aleks.bricksensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView compassImage;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnometer;
    private float currentCompassAngle = 0;
    private float[] readingMagnometer = new float[3];
    private float[] readingAccelerometer = new float[3];

    private float ALPHA = 0.15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        compassImage = (ImageView)findViewById(R.id.ivCompass);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_compass, menu);
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

    public void onBtnBackClick(View view) {
        finish();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnometer);
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
            readingAccelerometer = lowPass(event.values.clone(), readingAccelerometer);
        }
        if(event.sensor == magnometer)
        {
//            readingMagnometer[0] = event.values[0];
//            readingMagnometer[1] = event.values[1];
//            readingMagnometer[2] = event.values[2];
            readingMagnometer = lowPass(event.values.clone(), readingMagnometer);
        }

        SensorManager.getRotationMatrix(rotation, null, readingAccelerometer, readingMagnometer);
        SensorManager.getOrientation(rotation, orientation);
        float azimuthRadians = orientation[0];
        float azimuthDegrees = -(float) (Math.toDegrees(azimuthRadians) + 360) % 360;

        doAnimation(currentCompassAngle, azimuthDegrees, compassImage);

        currentCompassAngle = azimuthDegrees;
    }

    private void doAnimation(float from, float to, ImageView rotateImg) {

//        if (mCompassPositionDegree != mCompassDegree) {
//
//            float d = Math.abs(mCompassPositionDegree - mCompassDegree);
//
//            float s = (mCompassDegree - mCompassPositionDegree) / d;
//
//            if (d > 180) {
//
//                s = -s;
//
//                d = 360 - d;
//
//            }
//
//            mCompassPositionDegree += s * 0.3f * d;
//
//            if (mCompassPositionDegree < 0) {
//
//                mCompassPositionDegree = 360 - mCompassPositionDegree;
//
//            }
//
//        }

        RotateAnimation rotateAnimation = new RotateAnimation(
                from,
                to,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);
        rotateImg.startAnimation(rotateAnimation);
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
