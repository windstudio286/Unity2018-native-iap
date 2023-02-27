package vn.vplay.sdk.t000a;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.DisplayCutout;
import android.graphics.Rect;

import java.util.List;

public class Utils {
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
