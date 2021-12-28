package com.mufty.phony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class PhonyNotificationListenerService extends NotificationListenerService {
    private static String LOG_TAG = PhonyNotificationListenerService.class.getSimpleName();
    private NLServiceReceiver nlservicereciver;


    @Override
    public void onCreate() {
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mufty.phony.NOTIFICATION_LISTENER");
        registerReceiver(nlservicereciver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d( LOG_TAG, "Service bind");

        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(LOG_TAG,"**********  onNotificationPosted");
        Log.i(LOG_TAG,"ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "t" + sbn.getPackageName());
        Intent i = new  Intent("com.mufty.phony.NOTIFICATION_LISTENER");
        i.putExtra("notification_event","onNotificationPosted :" + sbn.getPackageName() + "n");
        sendBroadcast(i);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(LOG_TAG,"********** onNOtificationRemoved");
        Log.i(LOG_TAG,"ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText +"t" + sbn.getPackageName());
        Intent i = new  Intent("com.mufty.phony.NOTIFICATION_LISTENER");
        i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "n");

        sendBroadcast(i);
    }

    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null || intent.getStringExtra("command") == null) {
                return;
            }

            if(intent.getStringExtra("command").equals("clearall")){
                PhonyNotificationListenerService.this.cancelAllNotifications();
            }
            else if(intent.getStringExtra("command").equals("list")){
                Intent i1 = new  Intent("com.mufty.phony.NOTIFICATION_LISTENER");
                i1.putExtra("notification_event","=====================");
                sendBroadcast(i1);
                int i=1;
                for (StatusBarNotification sbn : PhonyNotificationListenerService.this.getActiveNotifications()) {
                    Intent i2 = new  Intent("com.mufty.phony.NOTIFICATION_LISTENER");
                    i2.putExtra("notification_event",i +" " + sbn.getPackageName() + "n");
                    sendBroadcast(i2);
                    i++;
                }
                Intent i3 = new  Intent("com.mufty.phony.NOTIFICATION_LISTENER");
                i3.putExtra("notification_event","===== Notification List ====");
                sendBroadcast(i3);

            }

        }
    }
}
