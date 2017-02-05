package com.example.roman.e2zaznamy.record;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.roman.e2zaznamy.R;


/**
 * Defines View for one {@link RecordItem}
 * <p>
 * Created by Skup on 5.2.2017.
 */

public class RecordItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

  //private static final DateFormat dateFormat = new SimpleDateFormat("dd. M. yyyy");
  private View parentView;
  private TextView textViewRecordName,
          textViewRecordCategory,
          textViewRecordDate;
  private ImageView imageViewRecordType;
  private RecordItem actualRecord;
  private OnRecordItemClickListener recordItemClickListener;
  private OnMenuItemClickListener<RecordItemViewHolder> menuItemClickListener;
  private int recordIndex;

  public RecordItemViewHolder(View itemView) {
    super(itemView);
    this.parentView = itemView;

    parentView.setOnClickListener(this);
    parentView.setOnCreateContextMenuListener(this);

    textViewRecordName = (TextView) itemView.findViewById(R.id.textViewRecordName);
    textViewRecordCategory = (TextView) itemView.findViewById(R.id.textViewRecordCategory);
    textViewRecordDate = (TextView) itemView.findViewById(R.id.textViewRecordDate);
    imageViewRecordType = (ImageView) itemView.findViewById(R.id.imageViewRecordType);
    actualRecord = null;
    recordIndex = -1;
  }

  public RecordItem getActualRecord() {
    return actualRecord;
  }

  public View getParentView() {
    return parentView;
  }

  public TextView getTextViewRecordName() {
    return textViewRecordName;
  }

  public void setData(String showName, RecordItem record, int index) {
    textViewRecordName.setText(record.getRecord().getName());
    textViewRecordCategory.setText(showName);
    textViewRecordDate.setText(record.getRecord().getTime());
    if (record.getType() == RecordType.Audio) {
      imageViewRecordType.setBackgroundResource(R.drawable.ic_music_circle);
    } else if (record.getType() == RecordType.Video) {
      imageViewRecordType.setBackgroundResource(R.drawable.ic_filmstrip);
    } else {
      imageViewRecordType.setBackgroundResource(0);
    }
    actualRecord = record;
    recordIndex = index;
  }

  public void setRecordItemClickListener(OnRecordItemClickListener recordItemClickListener) {
    this.recordItemClickListener = recordItemClickListener;
  }

  public void setMenuItemClickListener(OnMenuItemClickListener<RecordItemViewHolder> menuItemClickListener) {
    this.menuItemClickListener = menuItemClickListener;
  }

  @Override
  public void onClick(View view) {
    if (recordItemClickListener != null)
      recordItemClickListener.onRecordClick(actualRecord, recordIndex);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    MenuItem item = menu.add(0, R.id.context_action_detail, 0, R.string.context_action_detail);
    item.setOnMenuItemClickListener(this);
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    if (menuItemClickListener != null)
      return menuItemClickListener.onMenuItemClick(item, this);
    return false;
  }

}

