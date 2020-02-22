package cz.skup5.e2shows.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import cz.skup5.e2shows.MainActivity;

/**
 * Created by Skup on 18.2.2017.
 *
 * @author Skup5
 */
public class NetworkUtils {

  private NetworkUtils() {
  }

  private static Context getContext() {
    return MainActivity.getContext();
  }

  /**
   * Checks network connection
   *
   * @return <code>true</code> if and only if device is connected,
   * <code>false</code> otherwise
   */
  public static boolean isNetworkConnected() {
    ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo() != null;
  }
}
