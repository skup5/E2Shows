package cz.skup5.e2shows.ui.listener;

import java.util.List;

/**
 * Created by Skup on 19.2.2017.
 *
 * @author Skup5
 */
public interface OnErrorListener {

  /**
   * @param errors error messages
   */
  void onError(List<String> errors);
}
