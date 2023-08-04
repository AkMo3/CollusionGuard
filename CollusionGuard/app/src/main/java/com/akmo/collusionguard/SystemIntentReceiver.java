package com.akmo.collusionguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.Objects;

public class SystemIntentReceiver extends BroadcastReceiver {

    public static HashMap<String, MessageEntity> map = new HashMap<>();

    private static final String ACTION_RECORD_ACTIVITY = "com.akmo.collusionguard.action.RECORD_ACTIVITY";

    private static final String EXTRA_SENDER = "com.akmo.collusionguard.extra.SENDER";
    private static final String EXTRA_RECEIVER = "com.akmo.collusionguard.extra.RECEIVER";
    private static final String EXTRA_DATA= "com.akmo.collusionguard.extra.DATA";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Log.d("CollusionGuard", "Intent received at SystemIntentReceiver");
            final String action = intent.getAction();
            if (ACTION_RECORD_ACTIVITY.equals(action)) {

                String sender = intent.getStringExtra(EXTRA_SENDER);
                String receiver = intent.getStringExtra(EXTRA_RECEIVER);
                String data = intent.getStringExtra(EXTRA_DATA);
                Log.d("CollusionGuard", "Data received " + sender + ", " + receiver + ", " + data);

                if (!Objects.equals(sender, receiver) && !sender.contains("android") &&
                    !receiver.contains("android")) handleActionRecordActivity(context, sender, receiver, data);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRecordActivity(Context context, String sender, String receiver, String data) {
        if (map.containsKey(sender.concat(receiver))) {
            MessageEntity messageEntity = map.get(sender + receiver);
            messageEntity.dataList.add(data);
        }
        else {
            MessageEntity messageEntity = new MessageEntity(sender, receiver);
            messageEntity.dataList.add(data);
            map.put(sender.concat(receiver), messageEntity);
        }
        Log.d("CollusionGuard", "Map updated with data : " + sender + ", " + receiver + ", " + data +
                ", key: " + sender.concat(receiver));
        Intent intent = new Intent(MainActivity.ACTION_CUSTOM_ACTION);

        context.sendBroadcast(intent);
    }
}