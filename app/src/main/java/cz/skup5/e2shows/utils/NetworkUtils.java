package cz.skup5.e2shows.utils;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Skup on 18.2.2017.
 *
 * @author Skup5
 */
public class NetworkUtils {

    private NetworkUtils() {
    }

    /**
     * Checks network connection
     *
     * @param context
     * @return <code>true</code> if and only if device is connected,
     * <code>false</code> otherwise
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
