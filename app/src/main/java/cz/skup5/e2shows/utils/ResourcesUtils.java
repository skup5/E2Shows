package cz.skup5.e2shows.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import cz.skup5.e2shows.MainActivity;

/**
 * Created by Skup on 16.2.2017.
 *
 * @author Skup5
 */
public class ResourcesUtils {

  private ResourcesUtils() {
  }

  private static Context getContext() {
    return MainActivity.getContext();
  }

  /**
   * Returns a localized string from the application's package's default string table.
   *
   * @param resId Resource id for the string
   * @return The string data associated with the resource, stripped of styled text information
   */
  @NonNull
  public static String getString(int resId) {
    return getContext().getString(resId);
  }
}
