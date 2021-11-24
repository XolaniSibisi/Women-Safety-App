package woman.safety.com.background.others;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import woman.safety.com.MainActivity;
import woman.safety.com.R;
import woman.safety.com.background.Helper;
import woman.safety.com.background.PreferenceManager;

/**
 * Created by Xolani Sibisi on 15/12/2020.
 */

public class BackgroundService extends Service implements SensorEventListener {

    private static final String TAG = "Background";
    private String URL = "http://maps.google.com/?q=###";
    private static final int SHAKE_THRESHOLD = 3000;
    private static final int THRESHOLD_TIME = 20;
    private static final int MAIN_COUNT = 3;
    //Wake-Lock and Power Manger - Lock
    private PowerManager pm;
    private PowerManager.WakeLock wl;
    private WifiManager wm;
    private WifiManager.WifiLock wlock;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private int thresholdCount = 1;

    private boolean isGpsEnabled = false;

    private FusedLocationProviderClient mFusedClient;

    private TimerTask timerTask;
    private Timer timer;

    private long gTimeStamp;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startServices();
    }

    //    private void callTimer() {
//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(BackgroundService.this, "Timer Task", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        };
//    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        checkGPSEnabled();

//        Toast.makeText(this, "Toast", Toast.LENGTH_SHORT).show();

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                long curTime = System.currentTimeMillis();

                if ((curTime - lastUpdate) > 100) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;

                    float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                    Log.d(TAG, "Value  - " + TimeUnit.MILLISECONDS.toSeconds((curTime - gTimeStamp))
                            + "\n Thre - " + thresholdCount
                            + "\n Speed - " + speed);

//

                    if (speed > SHAKE_THRESHOLD) {


                        if (thresholdCount == 1) {
                            //Save Time Stamp
                            gTimeStamp = System.currentTimeMillis();
                            thresholdCount++;

                        } else {
                            //Check Difference Between Global and Current, if its inside or not 10 sec
                            //if inside then increase threshold count
                            //else thresholdCount = 1;

                            if (TimeUnit.MILLISECONDS.toSeconds((curTime - gTimeStamp)) < 10) {
                                thresholdCount++;
                            } else {
                                thresholdCount = 1;
                            }
                        }

                        if (PreferenceManager.getDataPreference(BackgroundService.this)) {
                            if (thresholdCount == MAIN_COUNT) {
                                thresholdCount = 1;
                                Log.d(TAG, "onSensorChanged: \n Speed = " + speed);
                                if (isGpsEnabled) {
                                    woman.safety.com.background.location.LocationServices locationServices
                                            = new woman.safety.com.background.location.LocationServices(BackgroundService.this);
                                    locationServices.StartLocationRequest();

                                    final long startTime = System.currentTimeMillis();

                                    if (timer != null & timerTask != null) {
                                        timer.cancel();
                                        timerTask.cancel();

                                        timer = null;
                                        timerTask = null;

                                    }

                                    timer = new Timer();
                                    timerTask = new TimerTask() {
                                        @Override
                                        public void run() {
                                            long timeElapsed = System.currentTimeMillis() - startTime;

                                            if (Helper.getmLocation() != null &&
                                                    Helper.getmLocation().getLongitude() != 0
                                                    && Helper.getmLocation().getLatitude() != 0) {

                                                Log.d(TAG, "run: Location Found");

                                                timer.cancel();
                                                timerTask.cancel();

                                                String LatLng = Helper.getmLocation().getLatitude() + "," + Helper.getmLocation().getLongitude();
                                                URL = URL.replace("###", LatLng);
                                                Log.d(TAG, URL);
                                                String message = getString(R.string.defaultMessage);
                                                message = message.concat(" My Location " + URL);

                                                Helper.SendSms(BackgroundService.this, message);

//                                                Toast.makeText(BackgroundService.this, "GPS", Toast.LENGTH_SHORT).show();

                                                Log.d(TAG, "Inside Location - " + message);
                                            }


                                            if (TimeUnit.MILLISECONDS.toSeconds(timeElapsed) == THRESHOLD_TIME) {
                                                Log.d(TAG, "run: Time Elapsed Value " + TimeUnit.MILLISECONDS.toSeconds(timeElapsed) + " Check" + (TimeUnit.MILLISECONDS.toSeconds(timeElapsed) == THRESHOLD_TIME));
                                                timer.cancel();
                                                timerTask.cancel();

//                                                Toast.makeText(BackgroundService.this, "Time Out", Toast.LENGTH_SHORT).show();

                                                Log.d(TAG, "Inside Not Location - " + getString(R.string.defaultMessage));

                                                Helper.SendSms(BackgroundService.this, getString(R.string.defaultMessage));
                                            }

                                        }

                                    };
                                    timer.schedule(timerTask, 0, 500);
                                } else {
                                    Toast.makeText(BackgroundService.this, "No GPS", Toast.LENGTH_SHORT).show();
                                    Helper.SendSms(BackgroundService.this, getString(R.string.defaultMessage));
                                }

                            }
                        }
                    }

                    last_x = x;
                    last_y = y;
                    last_z = z;
                }
            }
        }
    }

    public void Toast(final String mesg)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BackgroundService.this, mesg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand:");

        mFusedClient = LocationServices.getFusedLocationProviderClient(BackgroundService.this);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (senSensorManager != null) {
            Log.d(TAG, "onStartCommand: Sensor Set");
            senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            senSensorManager.registerListener(BackgroundService.this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.d(TAG, "onStartCommand: No Sensor Found");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if(senSensorManager != null)
//            senSensorManager.unregisterListener(this);
    }

    private void checkGPSEnabled() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mFusedClient.asGoogleApiClient(), builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        isGpsEnabled = true;
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        isGpsEnabled = false;
                        break;
                }
            }
        });
    }

    private void startServices() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        createNotificationChannel(getApplicationContext(), CHANNEL_ID);

        Notification notification = new NotificationCompat
                .Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("CRM")
                .setContentText("Auto Logout detection")
                .setContentIntent(pendingIntent)
                //.setPriority(Notification.PRIORITY_MIN)
                .setAutoCancel(true)
                .build();

        startForeground(123, notification);

    }

    public static String CHANNEL_ID = "WomenSafety";

    public static void createNotificationChannel(@NonNull Context context, @NonNull String CHANNEL_ID) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channelname";
            String description = "Channel desription";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
                Log.d("NotificationLog", "NotificationManagerNull");
            }
        }
    }
}
