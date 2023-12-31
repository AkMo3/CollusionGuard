package com.akmo.location_broadcast1;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class LocationBroadcast extends BroadcastReceiver implements LocationListener {
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    String loc = " Location: ";

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;

    @Override
    public void onReceive(Context context, Intent arg1) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

        Intent in = new Intent("com.example.collector");
        Log.d("LocationBroadcast", "Sending Data: " + loc);
        in.putExtra(Intent.EXTRA_TEXT, loc);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        in.setType("text/plain");
        context.startActivity(in);

    }

    @Override
    public void onLocationChanged(Location location)
    {
        loc = loc.concat("Latitude:");
        loc = loc.concat(Double.toString(location.getLatitude()));
        loc = loc.concat(", Longitude:");
        loc = loc.concat(Double.toString(location.getLongitude()));
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("LocationBroadcast", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("LocationBroadcast", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("LocationBroadcast","status");
    }
}
