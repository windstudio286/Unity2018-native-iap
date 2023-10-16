package vn.vplay.sdk.t000a;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.DisplayCutout;
import android.graphics.Rect;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class Utils {
    public static Bundle convertJsonToBundle(JSONObject json){
        Bundle bundle = new Bundle();
        try{
            Iterator<String> iterator = json.keys();
            while(iterator.hasNext()){
                String key = (String)iterator.next();
                Object value = json.get(key);
                switch(value.getClass().getSimpleName()){
                    case "String":
                        bundle.putString(key, (String) value);
                        break;
                    case "Integer":
                        bundle.putInt(key, (Integer) value);
                        break;
                    case "Long":
                        bundle.putLong(key, (Long) value);
                        break;
                    case "Boolean":
                        bundle.putBoolean(key, (Boolean) value);
                        break;
                    case "JSONObject":
                        bundle.putBundle(key, convertJsonToBundle((JSONObject) value));
                        break;
                    case "Float":
                        bundle.putFloat(key, (Float) value);
                        break;
                    case "Double":
                        bundle.putDouble(key, (Double) value);
                        break;
                    default:
                        bundle.putString(key, value.getClass().getSimpleName());
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return bundle;

    }
    public static void logFirebaseEvent(Activity activity,String eventName, Bundle params){
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(activity);
        if (firebaseAnalytics != null) {
            firebaseAnalytics.logEvent(eventName, params);
        }
    }
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    public static int[] getSafeInsets(Activity activity) {
        DisplayCutout displayCutout;
        List<Rect> rects;
        int[] safeInsets = {0, 0, 0, 0};
        if (!(activity == null || Build.VERSION.SDK_INT < 28 || (displayCutout = activity.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout()) == null || (rects = displayCutout.getBoundingRects()) == null || rects.size() == 0)) {
            safeInsets[0] = displayCutout.getSafeInsetBottom();
            safeInsets[1] = displayCutout.getSafeInsetLeft();
            safeInsets[2] = displayCutout.getSafeInsetRight();
            safeInsets[3] = displayCutout.getSafeInsetTop();
        }
        return safeInsets;
    }

    public static int getOrientation(Context ctx) {
        if (ctx instanceof Activity) {
            return ((Activity) ctx).getResources().getConfiguration().orientation;
        }
        if (ctx instanceof Application) {
            return ((Application) ctx).getResources().getConfiguration().orientation;
        }
        return 0;
    }
}
