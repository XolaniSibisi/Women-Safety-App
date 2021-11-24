package woman.safety.com.background.location;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by Xolani Sibisi on 15/11/2020.
 */
public class LocationServices extends LocationCallback implements OnCompleteListener<Location>,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final long UPDATE_INTERVAL = 10 * 1000;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 3;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private Double lat = 0.0, lng = 0.0;
    private Location mLastLocation;

    private FusedLocationProviderClient mFusedLocationClient;
    private Context context;

    private LocationCallback mLocationCallBack;

    @SuppressLint("MissingPermission")
    public LocationServices(Context con) {
        context = con;

        mLocationCallBack = new LocationCallback();

        mFusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context);

        buildGoogleApiClient();

    }

    @Override
    public void onComplete(@NonNull Task<Location> task) {
        if (task.isSuccessful() && task.getResult() != null) {
            mLastLocation = task.getResult();

            lat = mLastLocation.getLatitude();
            lng = mLastLocation.getLongitude();
        }
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }

    @SuppressLint("MissingPermission")
    public void StartLocationRequest(){
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,getPendingIntent());
    }

    private void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(com.google.android.gms.location.LocationServices.API)
                .build();
        createLocationRequest();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public void stopLocationRequest(){
        mFusedLocationClient.removeLocationUpdates(getPendingIntent());
    }
}
