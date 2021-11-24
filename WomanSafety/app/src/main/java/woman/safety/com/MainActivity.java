package woman.safety.com;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.material.snackbar.Snackbar;

import woman.safety.com.background.Helper;
import woman.safety.com.background.PreferenceManager;
import woman.safety.com.background.others.BackgroundService;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static final int PERMISSION_REQUEST_CODE = 111;
    public static final int BATTERY_OPTIMIZATION_CODE = 112;
    private static final int GPS_DAILOG = 113;
    private static final String TAG = "MainActivity";
    private boolean isBattery = false;

    //Views
    private Button btnSubmit, btnEdit, btnSave;
    private EditText firstMsgNumber, secondMsgNumber, callNumber;

    //Sensor Listener
    private static final int SHAKE_THRESHOLD = 3500;
    private static final int MAIN_COUNT = 3;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private int thresholdCount = 1;
    private FusedLocationProviderClient mGoogleApiClient;

    private Switch aSwitch;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize View
        firstMsgNumber = (EditText) findViewById(R.id.emer_first_number);
        secondMsgNumber = (EditText) findViewById(R.id.emer_second_number);
        callNumber = (EditText) findViewById(R.id.emer_call_number);
        aSwitch = (Switch) findViewById(R.id.play_switch);

        btnSubmit = findViewById(R.id.buttonSubmit);
        btnSave = findViewById(R.id.buttonSave);
        btnEdit = findViewById(R.id.buttonEdit);

        mGoogleApiClient = LocationServices.getFusedLocationProviderClient(this);

        /**
         * Emergency Details is Already Saved, So it can be Updated
         * Else Will Add
         */
        if (PreferenceManager.getDataPreference(MainActivity.this)) {

            setVisibilityGone(btnSubmit, btnSave);

            setVisibilityVisible(btnEdit, aSwitch);

            setSavedValues();

            StartBackgroundServices();

        } else {

            setVisibilityGone(btnSave, btnEdit);

            setVisibilityVisible(btnSubmit);

            setEnable(firstMsgNumber, secondMsgNumber, callNumber);
        }

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (senSensorManager != null) {
            Log.d(TAG, "onCreate: Sensor Set");
            senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            Log.d(TAG, "onCreate: No Sensor Found");
        }
    }

    private void setSavedValues() {
        Pair<String, String> pair = PreferenceManager.getMessageNumber(MainActivity.this);

        firstMsgNumber.setText(pair.first);
        secondMsgNumber.setText(pair.second);

        callNumber.setText(PreferenceManager.getCallNumber(MainActivity.this));

        setDisable(firstMsgNumber, secondMsgNumber, callNumber);
    }

    private void setVisibilityGone(View... views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }

    private void setEnable(View... views) {
        for (View view : views) {
            view.setEnabled(true);
        }
    }

    private void setDisable(View... views) {
        for (View view : views) {
            view.setEnabled(false);
        }
    }

    private void setVisibilityVisible(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showGPSEnableDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        senSensorManager.unregisterListener(MainActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            isBattery = true;
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validate(btnSubmit)) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                        setVisibilityGone(btnSubmit);
                        setVisibilityVisible(btnEdit, aSwitch);
                        setDisable(firstMsgNumber, secondMsgNumber, callNumber);
                        PreferenceManager.saveNumbers(MainActivity.this
                                , firstMsgNumber.getText().toString(), secondMsgNumber.getText().toString(), callNumber.getText().toString());
                        PreferenceManager.setDataPreference(MainActivity.this, true);

                        invalidateOptionsMenu();
                        StartBackgroundServices();
                    } else {
                        if (weHavePermission()) {
                            if (isBattery) {
                                setVisibilityGone(btnSubmit);
                                setVisibilityVisible(btnEdit , aSwitch);
                                setDisable(firstMsgNumber, secondMsgNumber, callNumber);
                                PreferenceManager.saveNumbers(MainActivity.this
                                        , firstMsgNumber.getText().toString(), secondMsgNumber.getText().toString(), callNumber.getText().toString());
                                PreferenceManager.setDataPreference(MainActivity.this, true);

                                invalidateOptionsMenu();

                                StartBackgroundServices();
                            } else {
                                batteryOptimizePermission();
                            }
                        } else {
                            requestPermission(PERMISSION_REQUEST_CODE);
                        }
                    }
                }
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibilityGone(btnEdit);
                setVisibilityVisible(btnSave);
                setEnable(firstMsgNumber, secondMsgNumber, callNumber);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validate(btnSave)) {
                    setVisibilityGone(btnSave);
                    setVisibilityVisible(btnEdit);
                    setDisable(firstMsgNumber, secondMsgNumber, callNumber);
                    PreferenceManager.saveNumbers(MainActivity.this
                            , firstMsgNumber.getText().toString(), secondMsgNumber.getText().toString(), callNumber.getText().toString());
                    PreferenceManager.setDataPreference(MainActivity.this, true);
                }
            }
        });

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    playSiren(true);
                else
                    playSiren(false);
            }
        });
    }

    private void playSiren(boolean playSiren) {
        try {
            Helper.StopAll();
        } catch (IllegalStateException ex) {

        }
        if (playSiren) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.siren);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } else {
            if(mediaPlayer!=null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }
            }
        }
    }

    private boolean Validate(View view) {
        if (TextUtils.isEmpty(firstMsgNumber.getText().toString())) {
            Snackbar.make(view, "Please Enter Number", Snackbar.LENGTH_SHORT).show();
            firstMsgNumber.requestFocus();
            return false;
        } else if (firstMsgNumber.getText().length() < 10) {
            Snackbar.make(view, "Please Enter Valid Mobile Number", Snackbar.LENGTH_SHORT).show();
            firstMsgNumber.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(secondMsgNumber.getText().toString())) {
            Snackbar.make(view, "Please Enter Number", Snackbar.LENGTH_SHORT).show();
            secondMsgNumber.requestFocus();
            return false;
        } else if (secondMsgNumber.getText().length() < 10) {
            Snackbar.make(view, "Please Enter Valid Mobile Number", Snackbar.LENGTH_SHORT).show();
            secondMsgNumber.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(callNumber.getText().toString())) {
            Snackbar.make(view, "Please Enter Number", Snackbar.LENGTH_SHORT).show();
            callNumber.requestFocus();
            return false;
        } else if (callNumber.getText().length() < 10) {
            Snackbar.make(view, "Please Enter Valid Mobile Number", Snackbar.LENGTH_SHORT).show();
            callNumber.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void StartBackgroundServices() {
        Intent intent1 = new Intent(MainActivity.this, BackgroundService.class);
        stopService(intent1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent1);
        }
        else
        {
            startService(intent1);
        }
    }

    private boolean weHavePermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission(int REQUEST_CODE) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE
                , Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE
                , Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                batteryOptimizePermission();
            } else {
                requestPermission(PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BATTERY_OPTIMIZATION_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    isBattery = true;
                    btnSubmit.performClick();
                    break;
                case RESULT_CANCELED:
                    isBattery = false;
                    batteryOptimizePermission();
                    break;
            }
        } else if (requestCode == GPS_DAILOG) {
            switch (resultCode) {
                case RESULT_OK:
                    break;
            }
        }
    }

    public void batteryOptimizePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = this.getPackageName();
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                isBattery = true;
                btnSubmit.performClick();
            } else {
                isBattery = false;
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivityForResult(intent, BATTERY_OPTIMIZATION_CODE);
            }
        } else {
            isBattery = true;
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

//                Log.d(TAG, "onSensorChanged: \n X = "+x+"\n Y = "+y+"\n Z = "+z);

                long curTime = System.currentTimeMillis();

                if ((curTime - lastUpdate) > 100) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;
                    if (((int) ((curTime / 1000) % 60) - (int) ((lastUpdate / 1000) % 60) < 10)) {

                        float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

//                    Log.d(TAG, "onSensorChanged: \n Count = " + thresholdCount +"\n Speed = " + speed);

                        if (speed > SHAKE_THRESHOLD) {
                            if (thresholdCount == MAIN_COUNT) {
                                thresholdCount = 1;
//                            Log.d(TAG, "onSensorChanged: \n Speed = " + speed + " \n Threshold" + SHAKE_THRESHOLD);
                                Helper.TaskDetailNotification(MainActivity.this, "Accelerometer has Changed");
//                            Helper.SendSms(BackgroundService.this);
                            } else {
                                thresholdCount++;
                            }
                        }
                    } else {
                        thresholdCount = 1;
                    }

                    Log.d(TAG, "onSensorChanged: \n Count = " + thresholdCount);

                    last_x = x;
                    last_y = y;
                    last_z = z;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void showGPSEnableDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient.asGoogleApiClient(), builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
//                        senSensorManager.registerListener(MainActivity.this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MainActivity.this, GPS_DAILOG);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.stop_services);
        if (PreferenceManager.getDataPreference(MainActivity.this)) {
            menuItem.setVisible(true);
            invalidateOptionsMenu();
        } else {
            menuItem.setVisible(false);
            invalidateOptionsMenu();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.stop_services) {
            showStopAlert();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showStopAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setPositiveButton("Stop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                stopServices();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void stopServices() {
        StartBackgroundServices();
        aSwitch.setChecked(false);
        Helper.StopAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            }
        }
    }
}

