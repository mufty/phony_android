package com.mufty.phony;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mufty.phony.gson.Message;
import com.mufty.phony.gson.Notification;
import com.mufty.phony.net.TcpClient;

public class MainActivity extends AppCompatActivity {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    TcpClient tcpClient;
    private static String LOG_TAG = MainActivity.class.getSimpleName();
    private NotificationReceiver receiver;
    public static String ACTION_NOTIFICATION = "notification";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private AlertDialog enableNotificationListenerAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new ConnectTask().execute("");

        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }

        receiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mufty.phony.NOTIFICATION_LISTENER");
        registerReceiver(receiver,filter);
    }

    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... strings) {
            //we create a TCPClient object
            tcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    Log.d( LOG_TAG, "Got message: " + message );
                    //this method calls the onProgressUpdate
                    //publishProgress(message);
                    publishProgress(message);
                }
            });
            tcpClient.run("192.168.1.10", 18561);

            return null;
        }
    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d( LOG_TAG, "------------------- onReceive ------------------");

            Message msg = new Message();
            msg.action = ACTION_NOTIFICATION;

            Notification note = new Notification();
            String message = intent.getStringExtra("notification_text");
            String title = intent.getStringExtra("notification_title");
            note.title = title;
            note.message = message;

            String data = gson.toJson(note);
            msg.data = data;

            String dataToSend = gson.toJson(msg);
            tcpClient.sendMessage(dataToSend);
        }
    }

}