package woman.safety.com.background;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Pair;
import android.widget.Toast;

import java.util.Random;

import woman.safety.com.MainActivity;
import woman.safety.com.R;

public class Helper {
    public static Location mLocation;
    private static final String CHANNEL_ID = "WomanSafety";
    static Vibrator vibrator;
    static MediaPlayer mediaPlayer;
    public static void TaskDetailNotification(@NonNull Context context, @NonNull String message) {

        int randomValue = new Random().nextInt(990);

        Intent homeIntent = new Intent(context, MainActivity.class);
        homeIntent.putExtra("NotificationId", randomValue);

        createNotificationChannel(context);

        PendingIntent pendingFinishIntent = PendingIntent.getActivity(context, randomValue, homeIntent, 0);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        builder.setContentIntent(pendingFinishIntent)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setTicker("Woman Safety") //For visually challenged people,will be audibly announced.
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Woman Safety")
                .setContentText(message);
        Notification n = builder.build();
        if (nm != null) {
            nm.notify("WomanSafety", randomValue, n);
        }
    }

    private static void createNotificationChannel(@NonNull Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("MissingPermission")
    private static void MakeCall(@NonNull Context context) {
        String dial = "tel:" + PreferenceManager.getCallNumber(context);
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(dial));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static void PlaySiren(@NonNull Context context) {
        if(mediaPlayer!=null)
        {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            }
        }
        mediaPlayer = MediaPlayer.create(context, R.raw.siren);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private static void vibrateDevice(@NonNull final Context context) {

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            if (vibrator != null) {
                long[] pattern = { 0, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500};
                vibrator.vibrate(pattern , -1);
//                vibrator.vibrate(VibrationEffect.createOneShot(2000, 10));
            }
        } else {
            if (vibrator != null) {
                long[] pattern = { 0, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500};
                vibrator.vibrate(pattern , -1);
//                vibrator.vibrate(2000);
            }
        }
        MakeCall(context);
    }

    public static void StopAll()
    {
        if(vibrator!=null)
        {
            vibrator.cancel();
            vibrator=null;
        }

        if(mediaPlayer!=null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            }
        }
    }

    public static void SendSms(@NonNull final Context context, @NonNull String Message) {
        Pair<String, String> numbers = PreferenceManager.getMessageNumber(context);
        SmsManager smsManager = SmsManager.getDefault();

        smsManager.sendTextMessage(numbers.first, null, Message, null, null);
        smsManager.sendTextMessage(numbers.second, null, Message, null, null);

//        Toast(context,"SMS Send - "+Message);

        PlaySiren(context);
        vibrateDevice(context);
    }

    public static void Toast(final Context con,final String mesg)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(con, mesg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static Location getmLocation() {
        return mLocation;
    }

}
