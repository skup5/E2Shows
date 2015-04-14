package com.example.roman.testapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.roman.testapp.jweb.Category;
import com.example.roman.testapp.jweb.Record;

import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by Roman on 14.4.2015.
 */
public class RecordsAdapter extends BaseAdapter {

    private Context context;
    private Category source;

    public RecordsAdapter(Context context, Category source){
        this.context = context;
        this.source = source;
    }

    /**
     *
     * @return <code>true</code> if new records are added, <code>false</code> otherwise
     */
    public boolean downloadNext(){
        DownloaderFactory.RecordsDownloader downloader = (DownloaderFactory.RecordsDownloader) DownloaderFactory
                .getDownloader(DownloaderFactory.Type.Records).execute(source);
        try {
            Set newRecords = downloader.get();
            return source.addRecords(newRecords);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Category getSource() {
        return source;
    }

    public void setSource(Category source) {
        this.source = source;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
       return this.source.getCountRecords();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Record getItem(int position) {
        return (Record) this.source.getRecords().toArray()[position];
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        //return getItem(position).getId();
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // inflate the layout for each item of listView
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.records_listview_item, null);

        Record record = getItem(position);

        // get the reference of textViews
        TextView textViewRecordName = (TextView) convertView.findViewById(R.id.textViewRecordName);
        TextView textViewRecordCategory = (TextView) convertView.findViewById(R.id.textViewRecordCategory);

        // Set data to respective TextViews
        textViewRecordName.setText(record.getName());
        textViewRecordCategory.setText(record.getCategory().getName());

        return convertView;
    }
}
