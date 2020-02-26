package cz.skup5.e2shows.utils;

import android.content.Context;

import cz.skup5.e2shows.activity.MainActivity;

/**
 * Created by Skup on 18.2.2017.
 *
 * @author Skup5
 */
public final class SystemServiceUtils {

  private SystemServiceUtils() {
  }

  private static final Context getContext() {
    return MainActivity.getContext();
  }

  /**
   * Return the handle to a system-level service by name. The class of the returned object varies by the requested name.
   *
   * @param serviceName The name of the desired service.
   * @return The service or null if the name does not exist.
   */
  public static final Object getSystemService(String serviceName) {
    return getContext().getSystemService(serviceName);
  }
}
