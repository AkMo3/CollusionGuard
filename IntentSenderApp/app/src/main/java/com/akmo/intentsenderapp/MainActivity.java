package com.akmo.intentsenderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_CUSTOM_ACTION = "com.akmo.collusionguard.action.RECORD_ACTIVITY";

    private static final String EXTRA_SENDER = "com.akmo.collusionguard.extra.SENDER";
    private static final String EXTRA_RECEIVER = "com.akmo.collusionguard.extra.RECEIVER";
    private static final String EXTRA_DATA= "com.akmo.collusionguard.extra.DATA";
    private static final String TAG = "IntentSenderApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText sender = findViewById(R.id.senderTextBox);
        EditText receiver = findViewById(R.id.receiverTextBox);
        EditText data = findViewById(R.id.dataTextBox);
        Button sendIntent = findViewById(R.id.sendIntentButton);

        sendIntent.setOnClickListener((view) -> {
            Intent intent = new Intent(ACTION_CUSTOM_ACTION);
            intent.putExtra(EXTRA_SENDER, sender.getText());
            intent.putExtra(EXTRA_RECEIVER, receiver.getText());
            intent.putExtra(EXTRA_DATA, data.getText());
            Log.d(TAG, "Data received " + sender.getText() + ", " + receiver.getText() + ", " + data.getText());
            sendBroadcast(intent);
        });
    }
}