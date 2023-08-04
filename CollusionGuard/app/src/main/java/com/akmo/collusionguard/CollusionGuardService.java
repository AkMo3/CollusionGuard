package com.akmo.collusionguard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class CollusionGuardService extends Service {

    private static final String CHANNEL_ID = "collusionguard_notification_channel";
    final int NOTIFICATION_ID = (int) (Math.random() * 1000);
    private SystemIntentReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();

        receiver = new SystemIntentReceiver();
        this.registerReceiver(receiver, new IntentFilter("com.akmo.collusionguard.action.RECORD_ACTIVITY"));

        // Create and show a notification for the foreground service
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        Log.d("CollusionGuard", "CollusionGuard App Service Started");
    }

    public void stopService() {
        // Stop the foreground service and remove the notification
        stopForeground(true);

        // Stop the service itself
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister the BroadcastReceiver when the service is destroyed
        unregisterReceiver(receiver);
    }

    // Method to create the notification for the foreground service
    private Notification createNotification() {
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("CollusionGuard")
                .setContentText("CollusionGuard Service Running")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        return builder.build();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
