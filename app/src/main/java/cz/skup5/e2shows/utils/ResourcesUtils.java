package cz.skup5.e2shows.utils;

import android.content.Context;
import androidx.annotation.NonNull;


/**
 * Created by Skup on 16.2.2017.
 *
 * @author Skup5
 */
public class ResourcesUtils {

  private ResourcesUtils() {
  }

  /**
   * Returns a localized string from the application's package's default string table.
   *
   * @param resId Resource id for the string
   * @return The string data associated with the resource, stripped of styled text information
   */
  @NonNull
  public static String getString(Context context, int resId) {
    return context.getString(resId);
  }
}
