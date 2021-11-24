package woman.safety.com.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;

import java.util.Objects;

import woman.safety.com.background.others.BackgroundService;


public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            try {
                if (PreferenceManager.getDataPreference(context)) {
                    startBackgroundServiceAndPerformLogin(context);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startBackgroundServiceAndPerformLogin(@NonNull Context appContext) {
        Intent intent1 = new Intent(appContext, BackgroundService.class);
        appContext.stopService(intent1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(intent1);
        }
        else
        {
            appContext.startService(intent1);
        }
    }
}
