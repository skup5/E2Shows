package cz.skup5.e2shows.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Skup on 19.2.2017.
 */

public class ArrayListView<T> extends ListView {

  private int lastClickedItemPosition = INVALID_POSITION;

  public ArrayListView(Context context) {
    super(context);
  }

  public ArrayListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ArrayListView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private void setLastClickedItemPosition(int lastClickedItemPosition) {
    this.lastClickedItemPosition = lastClickedItemPosition;
  }

  @Override
  public boolean performItemClick(View view, int position, long id) {
    setLastClickedItemPosition(position);
    return super.performItemClick(view, position, id);
  }

  public T getLastClickedItem() {
    return getArrayAdapter().getItem(getLastClickedItemPosition());
  }

  public int getLastClickedItemPosition() {
    return lastClickedItemPosition;
  }

  public ArrayAdapter<T> getArrayAdapter() {
    return (ArrayAdapter<T>) super.getAdapter();
  }

  public void setArrayAdapter(ArrayAdapter<T> adapter) {
    super.setAdapter(adapter);
  }

}
