package com.malino.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class MalinoSmsReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "malino_sms";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (messages == null || messages.length == 0) return;

        String sender = messages[0].getOriginatingAddress();
        StringBuilder body = new StringBuilder();
        for (SmsMessage m : messages) {
            if (m != null && m.getMessageBody() != null) body.append(m.getMessageBody());
        }
        if (sender == null) sender = "";

        try {
            File file = new File(context.getFilesDir(), "pending_sms.txt");
            String payload = sender + "\n" + body + "\n";
            FileOutputStream out = new FileOutputStream(file, false);
            out.write(payload.getBytes(StandardCharsets.UTF_8));
            out.close();
            notifyUser(context);
        } catch (Exception ignored) {
        }
    }

    private void notifyUser(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "پیامک‌های بانکی", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }
        Intent launch = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        PendingIntent pi = null;
        if (launch != null) {
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
            pi = PendingIntent.getActivity(context, 1001, launch, flags);
        }
        android.app.Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new android.app.Notification.Builder(context, CHANNEL_ID)
                : new android.app.Notification.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("مالینو")
                .setContentText("یک پیامک بانکی جدید دریافت شد")
                .setAutoCancel(true);
        if (pi != null) builder.setContentIntent(pi);
        nm.notify(1001, builder.build());
    }
}
