package com.akmo.receiverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomReceiver customReceiver = new CustomReceiver();
        this.registerReceiver(customReceiver, new IntentFilter("CUSTOM_RECEIVER_ACTION"));
        setContentView(R.layout.activity_main);

        Intent invokingIntent = getIntent();
        if (invokingIntent != null) {
            Bundle bundle = invokingIntent.getExtras();
            if (bundle != null) {
                String dataReceived = bundle.getString("DATA_FROM_SENDER");
                TextView textView = findViewById(R.id.textView);
                String stringToDisplay = "Data Received is: " + dataReceived;
                textView.setText(stringToDisplay);
            }
        }
    }
}