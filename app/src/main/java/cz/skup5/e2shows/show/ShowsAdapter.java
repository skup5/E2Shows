package cz.skup5.e2shows.show;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cz.skup5.e2shows.R;
import cz.skup5.e2shows.dto.ShowDto;

/**
 * Created by Roman on 16.6.2016.
 */
public class ShowsAdapter extends BaseAdapter {

  private ShowDto[] shows = new ShowDto[0];
  private LayoutInflater inflater;
  private int textColor = Color.TRANSPARENT;
  private int markItemColor;


  private int selectedItem = -1;


  public ShowsAdapter(Context context) {
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      this.markItemColor = context.getResources().getColor(R.color.colorPrimary);
  }

  @Override
  public int getCount() {
    return shows.length;
  }

  @Override
  public Object getItem(int i) {
    return shows[i];
  }

  @Override
  public long getItemId(int i) {
    return 0;
  }

  @Override
  public View getView(int i, View view, ViewGroup viewGroup) {
    TextView textView;
    view = inflater.inflate(R.layout.shows_list_item, null);
    textView = view.findViewById(R.id.shows_list_item);

    textView.setText(((ShowDto) getItem(i)).getName());

    if (textColor == Color.TRANSPARENT) {
      textColor = textView.getCurrentTextColor();
    }

    if (isItemSelected(i)) {
      markItem(textView);
    } else {
      unmarkItem(textView);
    }

    if (i == 0) {
      textView.setPadding(
        textView.getPaddingLeft(), textView.getPaddingBottom(),
        textView.getPaddingRight(), textView.getPaddingBottom()
      );
    }
    
//    Animation animation = AnimationUtils.makeInChildBottomAnimation(context);
//    animation.setDuration(100);
//    view.setAnimation(animation);
    return view;
  }

  private void unmarkItem(TextView textView) {
    textView.setTextColor(textColor);
  }

  private void markItem(TextView textView) {
    textView.setTextColor(markItemColor);
  }

  private boolean isItemSelected(int i) {
    return i == selectedItem;
  }

  public void setSelectedItem(int selectedItem) {
    this.selectedItem = selectedItem;
  }

  public void setShows(ShowDto[] shows) {
    this.shows = shows;
  }
}
