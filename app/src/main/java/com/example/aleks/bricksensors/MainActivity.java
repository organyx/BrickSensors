package com.example.aleks.bricksensors;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void onBtnCompassClick(View view) {
        Intent compassIntent = new Intent(MainActivity.this, CompassActivity.class);
        startActivity(compassIntent);
    }

    public void onBtnLightSaberClick(View view) {
        Intent lightSaberIntent = new Intent(MainActivity.this, LightSaberActivity.class);
        startActivity(lightSaberIntent);
    }

    public void onBtnGeigerClick(View view) {
        Intent geigerCounterIntent = new Intent(MainActivity.this, GeigerCounterActivity.class);
        startActivity(geigerCounterIntent);
    }
}
