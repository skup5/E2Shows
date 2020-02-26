package cz.skup5.e2shows.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import cz.skup5.e2shows.R;
import cz.skup5.e2shows.dto.ShowDto;
import cz.skup5.e2shows.exception.ShowLoadingException;
import cz.skup5.e2shows.manager.BasicShowManager;
import cz.skup5.e2shows.manager.ShowManager;
import cz.skup5.e2shows.ui.view.ArrayListView;

/**
 * Created by Skup on 19.2.2017.
 */
public class ShowArrayAdapter extends ArrayAdapter<ShowDto> {

  private final ShowManager showManager = BasicShowManager.getInstance();
  private final int markItemColor = Resources.getSystem().getColor(android.R.color.holo_blue_dark);

  private int textColor = Color.TRANSPARENT;

  public ShowArrayAdapter(Context context, int resource, int textViewResourceId) {
    super(context, resource, textViewResourceId);
  }

  /*### Override ArrayAdapter ###*/

  @NonNull
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = super.getView(position, convertView, parent);
    TextView textView = (TextView) view.findViewById(R.id.shows_list_item);

//    textView.setText(((ShowDto) getItem(i)).getName());

    if (textColor == Color.TRANSPARENT) {
      textColor = textView.getCurrentTextColor();
    }

    ArrayListView showsList = (ArrayListView) parent;
    if (showsList.getLastClickedItemPosition() == position) {
      markItem(textView);
    } else {
      unmarkItem(textView);
    }

//    Animation animation = AnimationUtils.makeInChildBottomAnimation(context);
//    animation.setDuration(100);
//    view.setAnimation(animation);
    return view;
  }

  /*### Public ###*/

  /**
   * Loads data in the adapter. Should by called out of UI thread.
   *
   * @throws ShowLoadingException
   */
  public void loadData() throws ShowLoadingException {
    addAll(showManager.loadAllShows());
  }

  /*### Private ###*/

  private void unmarkItem(TextView textView) {
    textView.setTextColor(textColor);
  }

  private void markItem(TextView textView) {
    textView.setTextColor(markItemColor);
  }
}
