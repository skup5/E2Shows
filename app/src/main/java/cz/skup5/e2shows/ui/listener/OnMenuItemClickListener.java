package cz.skup5.e2shows.ui.listener;

import android.view.MenuItem;

/**
 * Created by Skup on 5.2.2017.
 */
public interface OnMenuItemClickListener<T> {
  /**
   * @param item   The menu item that was invoked.
   * @param source For what the menu belongs
   * @return Return true to consume this click and prevent others from executing.
   */
  boolean onMenuItemClick(MenuItem item, T source);
}
