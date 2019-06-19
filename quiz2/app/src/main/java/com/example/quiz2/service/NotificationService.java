package com.example.quiz2.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.quiz2.MainActivity;
import com.example.quiz2.R;

import java.util.concurrent.TimeUnit;

public class NotificationService extends Service {

    private static final String TAG_FOREGROUND_SERVICE = "FOREGROUND_SERVICE";
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    private static final int NOTIF_ID = 2;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private RemoteViews mRemoteViews;
    private Notification mNotification;


    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG_FOREGROUND_SERVICE, "onBind");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG_FOREGROUND_SERVICE, "My foreground service onCreate().");
        startForegroundService();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            Log.e(TAG_FOREGROUND_SERVICE, "MonStartCommand");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Used to build and start foreground service.
      */
    private void startForegroundService() {
        setUpNotification();
        //Convert 5 minutes into milliseconds
        int noOfMinutes = Integer.parseInt("5") * 60 * 1000;
        updateNotification(noOfMinutes);
    }


    /**
     * call this method to setup notification for the first time
     */
    private void setUpNotification() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // we need to build a basic notification first, then update it
        Intent intentNotif = new Intent(this, MainActivity.class);
        intentNotif.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intentNotif, PendingIntent.FLAG_UPDATE_CURRENT);

        // notification's layout
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);

        mBuilder = new NotificationCompat.Builder(this);

        CharSequence ticker = "";
        int apiVersion = Build.VERSION.SDK_INT;

        if (apiVersion < Build.VERSION_CODES.HONEYCOMB) {
            mNotification = new Notification(R.drawable.ic_launcher_foreground, ticker, System.currentTimeMillis());
            mNotification.contentView = mRemoteViews;
            mNotification.contentIntent = pendIntent;

            //Do not clear the notification
            mNotification.flags |= Notification.FLAG_NO_CLEAR;
            mNotification.defaults |= Notification.DEFAULT_LIGHTS;

            // starting service with notification in foreground mode
            startForeground(NOTIF_ID, mNotification);

        } else if (apiVersion >= Build.VERSION_CODES.HONEYCOMB) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String NOTIFICATION_CHANNEL_ID = "com.example.quiz2";
                String channelName = "Notification Service";
                NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
                //chan.setLightColor(Color.BLUE);
                chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                mNotificationManager.createNotificationChannel(chan);
                mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

            }

            mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setContentIntent(pendIntent)
                    .setContent(mRemoteViews)
                    .setTicker(ticker);

            // starting service with notification in foreground mode
            startForeground(NOTIF_ID, mBuilder.build());
        }
    }

    /**
     * use this method to update the Notification's UI
     */
    private void updateNotification(int noOfMinutes) {
        new CountDownTimer(noOfMinutes, 1000) {
            public void onTick(long millisUntilFinished) {
                long millis = millisUntilFinished;
                String time = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                mRemoteViews.setTextViewText(R.id.txt_count, time);
                int api = Build.VERSION.SDK_INT;
                // update the notification
                if (api < Build.VERSION_CODES.HONEYCOMB) {
                    mNotificationManager.notify(NOTIF_ID, mNotification);
                } else if (api >= Build.VERSION_CODES.HONEYCOMB) {
                    mNotificationManager.notify(NOTIF_ID, mBuilder.build());
                }
            }

            public void onFinish() {
                stopSelf();
            }
        }.start();
    }


}