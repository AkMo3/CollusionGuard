package com.akmo.senderapp;

import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    // The Fused Location Provider provides access to location APIs.
    private FusedLocationProviderClient fusedLocationClient;

    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SMS_PERMISSION_REQUEST_CODE = 2;
    private String mText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestRequiredPermissions();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button intentButton = findViewById(R.id.send_intent_button);
        Button broadcastButton = findViewById(R.id.send_broadcast_button);
        Button forResultIntentButton = findViewById(R.id.send_for_result_intent_button);
        Button sendLocationButton = findViewById(R.id.send_location_button);
        Button sendSmsDataButton = findViewById(R.id.send_sms_data);
        Button safeModeButton = findViewById(R.id.safemode_test);

        safeModeButton.setOnClickListener((e) -> sendBroadcastToSafeMode());

        forResultIntentButton.setOnClickListener((view) -> {
            try {
                Intent sendNormalIntent = new Intent("CUSTOM_RECEIVER_ACTION");
                showInputDialog(MainActivity.this, sendNormalIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Activity not found", Toast.LENGTH_LONG).show();
            }
        });
        intentButton.setOnClickListener((view) -> {
            try {
                Intent sendNormalIntent = new Intent("CUSTOM_RECEIVER_ACTION");
                sendNormalIntent(sendNormalIntent, "Normal ");
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Activity not found", Toast.LENGTH_LONG).show();
            }
        });

        sendLocationButton.setOnClickListener((view) -> {
            try {
                Intent sendNormalIntent = new Intent("CUSTOM_RECEIVER_ACTION");
                sendNormalIntent(sendNormalIntent, "37.4219983, -122.084");
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Activity not found", Toast.LENGTH_LONG).show();
            }
        });

        broadcastButton.setOnClickListener((view) -> {
            Toast.makeText(getApplicationContext(), "Sender App: Broadcast Sent", Toast.LENGTH_SHORT).show();
            Intent broadcastIntent = new Intent("CUSTOM_RECEIVER_ACTION");
            broadcastIntent.putExtra("DATA_FROM_SENDER", "THIS DATA FROM BROADCAST");
//            broadcastIntent.setClassName("com.akmo.receiverapp", "CustomReceiver");
            sendBroadcast(broadcastIntent);
        });

        sendSmsDataButton.setOnClickListener((view) -> {
            try {
                Intent sendNormalIntent = new Intent("CUSTOM_RECEIVER_ACTION");
                getRecentSMS(sendNormalIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Activity not found", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendNormalIntent(Intent sendNormalIntent, String data) {
        sendNormalIntent.putExtra("DATA_FROM_SENDER", data);
        startActivity(sendNormalIntent);
    }

    private void showInputDialog(Context c, Intent intentToSend) {
        // Inflate the layout for the input dialog
        final EditText taskEditText = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Enter text to send")
                .setView(taskEditText)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mText = String.valueOf(taskEditText.getText());
                        sendNormalIntent(intentToSend, mText);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void requestRequiredPermissions() {
        // Permission is not granted
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void requestCurrentLocation(Intent intentToSend) {
        Log.d(TAG, "requestCurrentLocation()");
        final String[] stringToSend = {""};
        // Request permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            boolean sleep = true;

            // Main code
            Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.getToken()
            );

            currentLocationTask.addOnCompleteListener((task -> {
                if (task.isSuccessful()) {
                    // Task completed successfully
                    Location location = task.getResult();
                    stringToSend[0] = "Location (success): " +
                            location.getLatitude() +
                            ", " +
                            location.getLongitude();

                } else {
                    // Task failed with an exception
                    Exception exception = task.getException();
                    stringToSend[0] = "Exception thrown: " + exception;
                }

                sendNormalIntent(intentToSend, stringToSend[0]);
                Log.d(TAG, "getCurrentLocation() result: " + stringToSend[0]);
            }));
        } else {
            Log.d(TAG, "Request fine location permission.");

        }
        Log.d(TAG, "Location Received: " + stringToSend[0]);
    }

    private void getRecentSMS(Intent intentToSend) {
        // Define the Uri for the SMS inbox
        Uri uri = Uri.parse("content://sms/inbox");

        String smsData = "";

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {

            // Get the ContentResolver
            ContentResolver contentResolver = getContentResolver();

            // Define the columns you want to retrieve
            String[] projection = new String[]{"address", "body", "date"};

            // Query the SMS inbox using the ContentResolver
            Cursor cursor = contentResolver.query(uri, projection, null, null, "date DESC LIMIT 10");

            // Loop through the cursor and retrieve the SMS data
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Retrieve the SMS data from the cursor
                    if (cursor.getColumnIndex("address") > -1
                            && cursor.getColumnIndex("body") > -1
                            && cursor.getColumnIndex("date") > -1) {
                        String address = cursor.getString(cursor.getColumnIndex("address"));
                        String body = cursor.getString(cursor.getColumnIndex("body"));
                        long date = cursor.getLong(cursor.getColumnIndex("date"));

                        // Do something with the SMS data (e.g. display it in a TextView)
                        smsData = "From: " + address + "\n" + "Message: " + body + "\n" + "Date: " + date;
                        System.out.println(smsData);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        else {
            Log.d(TAG, "Request SMS permission.");
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_SMS)) {
                // Explain why permission is needed and ask for permission again
                new AlertDialog.Builder(this)
                        .setTitle("SMS permission needed")
                        .setMessage("This app needs the SMS permission to access your SMS.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Request permission again
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_SMS},
                                        SMS_PERMISSION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                // Request permission without explanation
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_SMS},
                        SMS_PERMISSION_REQUEST_CODE);
            }
        }


        sendNormalIntent(intentToSend, smsData);
    }

    private void sendBroadcastToSafeMode() {
        String safeModeAction = "com.akmo.safemode.action.RECORD_ACTIVITY";
        Intent intent = new Intent(safeModeAction);
        intent.putExtra("com.akmo.safemode.extra.SENDER", "testapp1");
        intent.putExtra("com.akmo.safemode.extra.RECEIVER", "testapp2");
        sendBroadcast(intent);
    }
}