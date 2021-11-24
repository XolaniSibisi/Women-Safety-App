package woman.safety.com.background;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.util.Pair;

public class PreferenceManager {
    private static final String SHARED_PREF = "WomanSafety";
    private static final String FIRST_NUMBER = "FirstNumber";
    private static final String SECOND_NUMBER = "SecondNumber";
    private static final String CALL_NUMBER = "CallNumber";
    private static final String EMERGENCY = "Emergency";
    private static final String IS_DATA_SAVED = "PreferenceSaved";


    public static void saveNumbers(@NonNull Context context, @NonNull String firstNumber
            , @NonNull String secondNumber, @NonNull String callNumber) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FIRST_NUMBER, firstNumber);
        editor.putString(SECOND_NUMBER, secondNumber);
        editor.putString(CALL_NUMBER, callNumber);
        editor.commit();
    }

    public static Pair<String, String> getMessageNumber(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return new Pair<String, String>(sharedPreferences.getString(FIRST_NUMBER, "")
                , sharedPreferences.getString(SECOND_NUMBER, ""));
    }

    public static String getCallNumber(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getString(CALL_NUMBER, "");
    }

    public static void setDataPreference(@NonNull Context context, boolean preference){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_DATA_SAVED, preference);
        editor.commit();
    }

    public static boolean getDataPreference(@NonNull Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(IS_DATA_SAVED, false);
    }

    public static void setEmergencyEnabled(@NonNull Context context, boolean emergency){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(EMERGENCY, emergency);
        editor.commit();
    }

    public static boolean getEmergency(@NonNull Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(EMERGENCY, false);
    }
}
