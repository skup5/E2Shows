package cz.skup5.e2shows.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cz.skup5.e2shows.R;
import cz.skup5.e2shows.dto.ShowDto;
import cz.skup5.e2shows.utils.SystemServiceUtils;

/**
 * Created by Roman on 16.6.2016.
 */
public class ShowsAdapter extends BaseAdapter {

  public static final int INVALID_POSITION = -1;

  private ShowDto[] shows = new ShowDto[0];
  private LayoutInflater inflater;
  private int textColor = Color.TRANSPARENT;
  private int markItemColor;

  private int selectedItemPosition = INVALID_POSITION;

  public ShowsAdapter() {
    this.inflater = (LayoutInflater) SystemServiceUtils.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.markItemColor = Resources.getSystem().getColor(android.R.color.holo_blue_dark);
  }

  /*### Override Adapter ###*/

  @Override
  public int getCount() {
    return shows.length;
  }

  @Override
  public Object getItem(int i) {
    if (i < 0 || i >= getCount()) return null;
    return shows[i];
  }

  @Override
  public long getItemId(int i) {
    return 0;
  }

  @Override
  public View getView(int i, View view, ViewGroup viewGroup) {
    TextView textView;
    view = inflater.inflate(R.layout.shows_listview_item, null);
    textView = (TextView) view.findViewById(R.id.shows_list_item);

    textView.setText(((ShowDto) getItem(i)).getName());

    if (textColor == Color.TRANSPARENT) {
      textColor = textView.getCurrentTextColor();
    }

    if (isItemSelected(i)) {
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

  public void clearSelection() {
    this.selectedItemPosition = INVALID_POSITION;
  }

  /**
   * @return the last selected item or null
   */
  public ShowDto getSelectedItem() {
    return (ShowDto) getItem(getSelectedItemPosition());
  }

  /**
   * @return position of last selected item or INVALID_POSITION
   */
  public int getSelectedItemPosition() {
    return selectedItemPosition;
  }

  public void setSelectedItemPosition(int position) {
    this.selectedItemPosition = position;
  }

  public void setShows(ShowDto[] shows) {
    this.shows = shows;
  }

  /*### Private ###*/

  private void unmarkItem(TextView textView) {
    textView.setTextColor(textColor);
  }

  private void markItem(TextView textView) {
    textView.setTextColor(markItemColor);
  }

  private boolean isItemSelected(int i) {
    return i == selectedItemPosition;
  }

}
