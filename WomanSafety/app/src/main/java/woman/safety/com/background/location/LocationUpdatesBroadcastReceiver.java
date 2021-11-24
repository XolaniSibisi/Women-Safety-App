/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package woman.safety.com.background.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;

import java.util.List;

import woman.safety.com.R;
import woman.safety.com.background.Helper;
import woman.safety.com.background.others.BackgroundService;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LBroadcastReceiver";

    static final String ACTION_PROCESS_UPDATES = "PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Log.d(TAG, "onReceive:");
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    Log.d(TAG, "onReceive: Result");
                    List<Location> mLocations = result.getLocations();
                    Helper.mLocation = mLocations.get(0);
                    Log.d(TAG, "onReceive: " + mLocations.get(0).getLatitude() + "," + mLocations.get(0).getLongitude());

                } else {
                    Log.d(TAG, "onReceive: Result Null");
                }
            }
        }
    }
}
