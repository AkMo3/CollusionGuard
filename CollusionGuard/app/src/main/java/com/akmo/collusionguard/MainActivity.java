package com.akmo.collusionguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_CUSTOM_ACTION = "com.akmo.collusionguard.action.ACTION_CUSTOM_ACTION";
    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent batteryPermissionIntent = new Intent();

        batteryPermissionIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        batteryPermissionIntent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
        startActivity(batteryPermissionIntent);

        Intent serviceIntent = new Intent(this, CollusionGuardService.class);
        startService(serviceIntent);

        myBroadcastReceiver = new MyBroadcastReceiver(this);
        this.registerReceiver(myBroadcastReceiver, new IntentFilter(ACTION_CUSTOM_ACTION));
        findViewById(R.id.update_list).setOnClickListener(this::updateList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ACTION_CUSTOM_ACTION);
        registerReceiver(myBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void updateList(View view) {
        LinearLayout layout = findViewById(R.id.linearLayout);
        updateAppMessageLayout(this, layout);
    }

    void updateAppMessageLayout(@NonNull Context context, @NonNull LinearLayout layout) {
        layout.removeAllViewsInLayout();

        HashMap<String, MessageEntity> map = SystemIntentReceiver.map;

        map.forEach ((key, messageEntity) ->  {

            LinearLayout.LayoutParams nestedLayoutParam = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            LinearLayout nestedLayout = new LinearLayout(context);
            nestedLayout.setLayoutParams(nestedLayoutParam);

            TextViewStyle textViewStyle = new TextViewStyle(context);

            TextView source = new TextView(context);
            source.setText(messageEntity.sender);
            textViewStyle.setTextViewStyle(source);

            TextView dest = new TextView(context);
            dest.setText(messageEntity.receiver);
            textViewStyle.setTextViewStyle(dest);

            TextView count = new TextView(context);
            count.setText(String.format("%d", messageEntity.dataList.size()));
            textViewStyle.setTextViewStyle(count);

            nestedLayout.addView(source);
            nestedLayout.addView(dest);
            nestedLayout.addView(count);

            nestedLayout.setOnClickListener(e -> startDataViewActivity(messageEntity.sender, messageEntity.receiver));
            layout.addView(nestedLayout);
        });
    }

    private void startDataViewActivity(String sender, String receiver) {
        Intent dataViewStartIntent = new Intent(this, DataViewingActivity.class);
        dataViewStartIntent.putExtra("Sender", sender);
        dataViewStartIntent.putExtra("Receiver", receiver);
        startActivity(dataViewStartIntent);
    }

    private class TextViewStyle {
        protected int leftPadding, rightPadding, topPadding, bottomPadding;

        TextViewStyle(Context context) {
            int paddingValue = 16; // Value in pixels
            int padding = (int) (paddingValue * context.getResources().getDisplayMetrics().density); // Convert to pixels
            leftPadding = padding;
            topPadding = padding;
            rightPadding = padding;
            bottomPadding = padding;

            int[] attrs = new int[]{android.R.attr.padding};
            TypedArray typedArray = context.obtainStyledAttributes(attrs);
            this.leftPadding = typedArray.getDimensionPixelSize(0, leftPadding);
            this.topPadding = typedArray.getDimensionPixelSize(1, topPadding);
            this.rightPadding = typedArray.getDimensionPixelSize(2, rightPadding);
            this.bottomPadding = typedArray.getDimensionPixelSize(3, bottomPadding);
            typedArray.recycle();
        }

        public void setTextViewStyle(TextView view) {
            view.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        private final Activity activity;

        public MyBroadcastReceiver(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CollusionGuard", "Intent received at Main Activity");
            if (intent.getAction().equals(ACTION_CUSTOM_ACTION)) {
                // Call the desired method in MainActivity
                LinearLayout layout = findViewById(R.id.linearLayout);
                updateAppMessageLayout(activity, layout);
            }
        }
    }
}