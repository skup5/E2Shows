package cz.skup5.e2shows.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import cz.skup5.e2shows.activity.MainActivity;

/**
 * Created by Skup on 18.2.2017.
 *
 * @author Skup5
 */
public final class NetworkUtils {

  private NetworkUtils() {
  }

  private static final Context getContext() {
    return MainActivity.getContext();
  }

  /**
   * Checks network connection
   *
   * @return <code>true</code> if and only if device is connected,
   * <code>false</code> otherwise
   */
  public static final boolean isNetworkConnected() {
    ConnectivityManager cm = (ConnectivityManager) SystemServiceUtils.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo ni = cm.getActiveNetworkInfo();
    return ni != null;
  }
}
