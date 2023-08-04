package com.akmo.location1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    String loc = "";
    Button getLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLoc = findViewById(R.id.button);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        getLoc.setOnClickListener(this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        loc = loc.concat("Latitude:");
        loc = loc.concat(Double.toString(location.getLatitude()));
        loc = loc.concat(", Longitude:");
        loc = loc.concat(Double.toString(location.getLongitude()));
    }

    @Override
    public void onClick(View v) {
        Intent in = new Intent("com.akmo.collector");
        Log.d("LocationApp1", "LocationApp1 sends data " + loc);
        in.putExtra(Intent.EXTRA_TEXT, loc);
        in.setType("text/plain");
        startActivity(in);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("LocationApp1", "Latitude - disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("LocationApp1", "Latitude - enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("LocationApp1", "Latitude - status");
    }
}