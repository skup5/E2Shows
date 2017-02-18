package cz.skup5.e2shows.record;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import cz.skup5.e2shows.dto.ShowDto;
import cz.skup5.e2shows.playlist.Playlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cz.skup5.e2shows.R;

/**
 * A Adapter used to provide data and ViewHolders from records to an RecyclerView.
 *
 * @author Roman Zelenik
 */
public class RecordsAdapter extends RecyclerView.Adapter<RecordItemViewHolder> implements Filterable, Playlist<RecordItem> {

  public static final int SELECTED_NONE = -1;
  private static final int ITEM_MARK_BG = android.R.color.holo_blue_dark;

  private Context context;
  private ShowDto source;
  private OnRecordItemClickListener onRecordClickListener;
  private OnMenuItemClickListener<RecordItemViewHolder> onMenuClickListener;
  //  private RecordItem[] publicRecords = new RecordItem[0];
  private ArrayList<RecordItem> publicRecords;
  private RecordType filterType = RecordType.All;
  private Drawable itemBackground;
  private Filter filter;
  private int selected;
  private boolean loading;

  public RecordsAdapter(Context context) {
    this(context, null);
  }

  public RecordsAdapter(Context context, ShowDto source) {
    this.context = context;
    this.loading = false;
    this.selected = SELECTED_NONE;
    this.filter = initFilter();
    initData();
    if (source != null) setSource(source);
  }

  /*#######################################################
    ###              PRIVATE METHODS                    ###
    #######################################################*/

  /**
   * Returns index of selected item or {@link #SELECTED_NONE}
   */
  private int getSelected() {
    return selected;
  }

  private boolean isSelected(int position) {
    return position == getSelected();
  }

  private int indexOfItem(RecordItem item) {
    return publicRecords.indexOf(item);
  }

  /**
   * Initializes {@code publicRecords}
   */
  private void initData() {
    publicRecords = new ArrayList<>();
  }

  private void setData(Collection<RecordItem> newData) {
    publicRecords.clear();
    publicRecords.addAll(newData);
  }

  private Filter initFilter() {
    return new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        String type = charSequence.toString();
        List<RecordItem> values = new ArrayList<>();
        if (hasSource()) {
          try {
            values.addAll(getSource().getRecordItemsWithType(RecordType.valueOf(type)));
          } catch (IllegalArgumentException e) {
            e.printStackTrace();
          }
        }
        results.values = values;
        results.count = values.size();
        return results;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        setData((List<RecordItem>) filterResults.values);
//          notifyItemRangeInserted(0, filterResults.count);
        notifyDataSetChanged();
      }
    };
  }

  /*#######################################################
    ###               PUBLIC METHODS                    ###
    #######################################################*/

  public void filter(RecordType type) {
    this.filterType = type;
    getFilter().filter(type.name());
  }

  /**
   * Returns {@link RecordItem} from actual records at specific index.
   *
   * @param index index in array
   * @return item or null
   */
  public RecordItem getItem(int index) {
    if (isEmpty() || index < 0 || index >= publicRecords.size())
      return null;
    return publicRecords.get(index);
  }

  public ShowDto getSource() {
    return source;
  }

  public boolean hasSource() {
    return source != null;
  }

  public boolean isEmpty() {
    if (hasSource()) {
      publicRecords.isEmpty();
    }
    return false;
  }

  public void markViewHolder(RecordItemViewHolder viewHolder) {
    viewHolder.getParentView().setBackgroundResource(ITEM_MARK_BG);
    //viewHolder.getTextViewRecordName().setTypeface(null, Typeface.ITALIC);
  }

  public void setSelected(int selected) {
    if (isEmpty() || selected < 0 || selected >= getItemCount()) {
      this.selected = SELECTED_NONE;
      return;
    }
    this.selected = selected;
  }

  public void setSource(ShowDto source) {
    this.source = source;
    /*SortedSet<RecordItem> recs = new TreeSet<>(source.getAudioRecords());
    recs.addAll(source.getVideoRecords());
    publicRecords = recs.toArray(new RecordItem[recs.size()]);
    */
    filter(filterType);
    //notifyDataSetChanged();
  }

  public void setOnRecordClickListener(OnRecordItemClickListener onRecordClickListener) {
    this.onRecordClickListener = onRecordClickListener;
  }

  public void setOnMenuClickListener(OnMenuItemClickListener<RecordItemViewHolder> onMenuClickListener) {
    this.onMenuClickListener = onMenuClickListener;
  }

  public void unmarkViewHolder(RecordItemViewHolder viewHolder) {
    viewHolder.getParentView().setBackground(itemBackground);
    //viewHolder.getTextViewRecordName().setTypeface(null, Typeface.NORMAL);
  }

  public void update() {
    setSource(this.source);
  }

  /*### Override RecyclerView.Adapter ###*/

  @Override
  public RecordItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.records_listview_item, null);
    RecordItemViewHolder holder = new RecordItemViewHolder(v);
    holder.setRecordItemClickListener(onRecordClickListener);
    holder.setMenuItemClickListener(onMenuClickListener);
    itemBackground = v.getBackground();
    return holder;
  }

  @Override
  public void onBindViewHolder(RecordItemViewHolder holder, int position) {
    if (!isEmpty()) {
      holder.setData(getSource().getName(), publicRecords.get(position), position);
      if (isSelected(position)) {
        markViewHolder(holder);
      } else {
        unmarkViewHolder(holder);
      }
    }
  }

  /**
   * Returns the total number of items in the data set hold by the adapter.
   *
   * @return The total number of items in this adapter.
   */
  @Override
  public int getItemCount() {
    return publicRecords.size();
  }

  /*### Override Filterable ###*/

  @Override
  public Filter getFilter() {
    return this.filter;
  }

  /*### Override Playlist ###*/

  @Override
  public RecordItem actual() {
    return getItem(getSelected());
  }

  @Override
  public RecordItem next() {
    RecordItem actual = actual();
    if (actual == null) {
      return first();
    }
    return getItem(getSelected() + 1);
  }

  @Override
  public RecordItem previous() {
    RecordItem actual = actual();
    if (actual == null) {
      return null;
    }
    return getItem(getSelected() - 1);
  }

  @Override
  public RecordItem first() {
    return getItem(0);
  }

  @Override
  public RecordItem last() {
    return getItem(getItemCount() - 1);
  }

  @Override
  public int indexOf(RecordItem item) {
    return indexOfItem(item);
  }
}