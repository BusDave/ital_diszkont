package com.example.teszt;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHandler {

    private static final String CHANNEL_ID = "shop_notification_channel";
    private final int NOTIFICATION_ID = 0;
    private NotificationManager mManager;
    private Context mContext;
    public NotificationHandler(Context context){
        this.mContext = context;
        this.mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannel();
    }

    private void createChannel(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        NotificationChannel channel =
                new NotificationChannel(CHANNEL_ID,"Shop notificatio",
                        NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setDescription("Notification from Drink application.");
        this.mManager.createNotificationChannel(channel);
    }

    public void send(String message){


        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext,CHANNEL_ID)
                .setContentTitle("Shop application")
                .setContentText(message)
                .setSmallIcon(R.drawable.baseline_add_shopping_cart_24);
        this.mManager.notify(NOTIFICATION_ID, builder.build());
        // noti epitese
    }
    public void cancel(){
        this.mManager.cancel(NOTIFICATION_ID);
    }
}
