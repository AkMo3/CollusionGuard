package com.akmo.receiverapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class CustomReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String data = intent.getExtras().getString("DATA_FROM_SENDER");
        Intent callMainActivity = new Intent(context, MainActivity.class);
        callMainActivity.putExtra("DATA_FROM_SENDER", data);
        context.startActivity(callMainActivity);
    }
}
