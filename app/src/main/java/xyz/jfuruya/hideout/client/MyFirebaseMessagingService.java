package xyz.jfuruya.hideout.client;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FIREBASE_MESSAGING_SERVICE";

    private static final String CHANNEL_ID = "hideout";
    //CharSequence name = getString(R.string.channel_name);
    private static final CharSequence NAME = "Hideout Channel";
    //String description = getString(R.string.channel_description);
    private static final String DESCRIPTION = "これはサンプルです。";

    public MyFirebaseMessagingService() {
        // Get token
        // [START retrieve_current_token]
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            private static final String TAG = "";

            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                String token = task.getResult().getToken();
                Log.d(TAG, token);

                // Log and toast
                //String msg = getString(R.string.msg_token_fmt, token);
                //Log.d(TAG, msg);
                //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
        // [END retrieve_current_token]
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "PUSH_DATA_RECEIVED!");
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // 通知チャネルを作成する
        this.createNotificationChannel();

        // 通知チャネルを取得する
        NotificationChannel channel = this.getNotificationChannel();

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("EXTRA_CHANNEL_ID_HIDEOUT", channel.getId());
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP  // 起動中のアプリがあってもこちらを優先する
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED  // 起動中のアプリがあってもこちらを優先する
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS  // 「最近利用したアプリ」に表示させない
            );
            PendingIntent contentIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(android.R.drawable.sym_def_app_icon)
                            .setContentTitle(remoteMessage.getData().toString())
                            .setContentText("Notification Message")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("Notification Message"));
            mBuilder.setContentIntent(contentIntent);
            notificationManager.notify(0, mBuilder.build());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
        } else {
            Log.d(TAG, "Data Size: " + remoteMessage.getData().size());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    private void handleNow() {
    }

    private void scheduleJob() {
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    /**
     * DBにTokenを送信する
     *
     * @param token
     */
    private void sendRegistrationToServer(String token) {
    }

    @SuppressLint("LongLogTag")
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, NAME, NotificationManager.IMPORTANCE_DEFAULT);
            Log.d(TAG, "Channel Id: " + CHANNEL_ID);

            channel.setDescription(DESCRIPTION);
            channel.enableLights(true); // 通知時にライトを有効にする
            channel.setLightColor(Color.WHITE); // 通知時のライトの色
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); // ロック画面での表示レベル

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        } else {
            Log.d(TAG, "createNotificstionChannel: " + Build.VERSION.SDK_INT + ":" + Build.VERSION_CODES.O);
        }
    }

    /**
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private NotificationChannel getNotificationChannel(){
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return notificationManager.getNotificationChannel(CHANNEL_ID);
        } else {
            return null;
        }
    }
}
