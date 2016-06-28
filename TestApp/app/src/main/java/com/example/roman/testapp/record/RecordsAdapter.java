package com.example.roman.testapp.record;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.example.roman.testapp.R;
import com.example.roman.testapp.show.ShowItem;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A Adapter used to provide data and ViewHolders from records to an RecyclerView.
 *
 * @author Roman Zelenik
 */
public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.MyViewHolder> implements Filterable {

  private static final int ITEM_MARK_BG = android.R.color.holo_blue_dark;

  private Context context;
  private ShowItem source;
  private OnRecordClickListener onRecordClickListener;
  private RecordItem[] publicRecords = new RecordItem[0];
  private Drawable itemBackground;
  private Filter filter;
  private int selected;
  private boolean loading;

  public RecordsAdapter(Context context, OnRecordClickListener listener) {
    this(context, listener, null);
  }

  public RecordsAdapter(Context context, OnRecordClickListener listener, ShowItem source) {
    this.context = context;
    this.onRecordClickListener = listener;
    this.loading = false;
    this.selected = -1;
    if (source != null) setSource(source);
    this.filter = initFilter();
  }

  public void downloadNext() {
    if (hasSource() && !loading) {
            /*if(source.hasNextRecords()) {
                DownloaderFactory.NextRecordsDownloader downloader = (DownloaderFactory.NextRecordsDownloader) DownloaderFactory
                        .getDownloader(DownloaderFactory.Type.NextRecords);
                downloader.setOnCompleteListener(new DownloaderFactory.OnCompleteListener() {
                    @Override
                    public void onComplete(Object result) {
                        if(result instanceof Category) {
                            setSource((Category) result);
                        }
                        loading = false;
                    }
                });
                downloader.setOnErrorListener(new DownloaderFactory.OnErrorListener() {
                    @Override
                    public void onError(List<String> errors) {
                        MainActivity.errorReportsDialog(context, errors);
                    }
                });
                downloader.execute(source);
                loading = true;
            }*/
    }
  }

  public RecordItem getItem(int index) {
    return index >= 0 && index < publicRecords.length ? publicRecords[index] : null;
  }

  public ShowItem getSource() {
    return source;
  }

  public boolean hasSource() {
    return source != null;
  }

  public boolean isEmpty() {
    if (hasSource()) {
      return publicRecords.length == 0;
    }
    return false;
  }

  public void markViewHolder(MyViewHolder viewHolder) {
    viewHolder.getParentView().setBackgroundResource(ITEM_MARK_BG);
    viewHolder.getTextViewRecordName().setTypeface(null, Typeface.ITALIC);
  }

  public void setSelected(int selected) {
    this.selected = selected;
  }

  public void setSource(ShowItem source) {
    this.source = source;
    SortedSet<RecordItem> recs = new TreeSet<>(source.getAudioRecords());
    recs.addAll(source.getVideoRecords());
    publicRecords = recs.toArray(new RecordItem[recs.size()]);
    notifyDataSetChanged();
  }

  public void unmarkViewHolder(MyViewHolder viewHolder) {
    viewHolder.getParentView().setBackground(itemBackground);
    viewHolder.getTextViewRecordName().setTypeface(null, Typeface.NORMAL);
  }

  public void update(){
    setSource(this.source);
}
  /**
   * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
   * an item.
   * <p>
   * This new ViewHolder should be constructed with a new View that can represent the items
   * of the given type. You can either create a new View manually or inflate it from an XML
   * layout file.
   * <p>
   * The new ViewHolder will be used to display items of the adapter using
   * {@link #onBindViewHolder(ViewHolder, int)}. Since it will be re-used to display different
   * items in the data set, it is a good idea to cache references to sub views of the View to
   * avoid unnecessary {@link View#findViewById(int)} calls.
   *
   * @param parent   The ViewGroup into which the new View will be added after it is bound to
   *                 an adapter position.
   * @param viewType The view type of the new View.
   * @return A new ViewHolder that holds a View of the given view type.
   * @see #getItemViewType(int)
   * @see #onBindViewHolder(ViewHolder, int)
   */
  @Override
  public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.records_listview_item, null);
    MyViewHolder holder = new MyViewHolder(v, onRecordClickListener);
    itemBackground = v.getBackground();
    return holder;
  }

  /**
   * Called by RecyclerView to display the data at the specified position. This method
   * should update the contents of the {@link ViewHolder#itemView} to reflect the item at
   * the given position.
   * <p>
   * Note that unlike {@link ListView}, RecyclerView will not call this
   * method again if the position of the item changes in the data set unless the item itself
   * is invalidated or the new position cannot be determined. For this reason, you should only
   * use the <code>position</code> parameter while acquiring the related data item inside this
   * method and should not keep a copy of it. If you need the position of an item later on
   * (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will have
   * the updated adapter position.
   *
   * @param holder   The ViewHolder which should be updated to represent the contents of the
   *                 item at the given position in the data set.
   * @param position The position of the item within the adapter's data set.
   */
  @Override
  public void onBindViewHolder(MyViewHolder holder, int position) {
    if (!isEmpty()) {
      holder.setData(getSource().getShow().getName(), publicRecords[position], position);
      if (isSelected(position)) {
        markViewHolder(holder);
      } else {
        unmarkViewHolder(holder);
      }
    }
  }

  private boolean isSelected(int position) {
    return position == selected;
  }

  /**
   * Returns the total number of items in the data set hold by the adapter.
   *
   * @return The total number of items in this adapter.
   */
  @Override
  public int getItemCount() {
    return publicRecords.length;
  }

  @Override
  public Filter getFilter() {
    return this.filter;
  }

  private Filter initFilter(){
    return new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        String type = charSequence.toString();
        String a = RecordItem.Type.Audio.name();
        String v = RecordItem.Type.Video.name();
        results.values = publicRecords;
        if (hasSource()) {
          if (type.compareTo(a) == 0) {
            results.values = getSource().getAudioRecords().toArray(new RecordItem[getSource().getAudioRecords().size()]);
          } else if (type.compareTo(v) == 0) {
            results.values = getSource().getVideoRecords().toArray(new RecordItem[getSource().getVideoRecords().size()]);
          }
        }
        results.count = ((RecordItem[]) results.values).length;
        return results;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        publicRecords = (RecordItem[]) filterResults.values;
        notifyDataSetChanged();
      }
    };
  }

  public static class MyViewHolder extends ViewHolder implements View.OnClickListener {

    //private static final DateFormat dateFormat = new SimpleDateFormat("dd. M. yyyy");
    private View parentView;
    private TextView textViewRecordName,
        textViewRecordCategory,
        textViewRecordDate;
    private ImageView imageViewRecordType;
    private RecordItem actualRecord;
    private OnRecordClickListener listener;
    private int recordIndex;

    public MyViewHolder(View itemView, OnRecordClickListener listener) {
      super(itemView);
      this.listener = listener;
      this.parentView = itemView;

      parentView.setOnClickListener(this);
      textViewRecordName = (TextView) itemView.findViewById(R.id.textViewRecordName);
      textViewRecordCategory = (TextView) itemView.findViewById(R.id.textViewRecordCategory);
      textViewRecordDate = (TextView) itemView.findViewById(R.id.textViewRecordDate);
      imageViewRecordType = (ImageView) itemView.findViewById(R.id.imageViewRecordType);
      actualRecord = null;
      recordIndex = -1;
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
      if(record.getType() == RecordItem.Type.Audio){
        imageViewRecordType.setBackgroundResource(android.R.drawable.presence_audio_online);
      }else if(record.getType() == RecordItem.Type.Video){
        imageViewRecordType.setBackgroundResource(android.R.drawable.presence_video_online);
      }else {
        imageViewRecordType.setBackgroundResource(0);
      }
      actualRecord = record;
      recordIndex = index;
    }

    @Override
    public void onClick(View view) {
      listener.onRecordClick(actualRecord, recordIndex);
    }
  }

  public interface OnRecordClickListener {
    void onRecordClick(RecordItem record, int index);
  }
}