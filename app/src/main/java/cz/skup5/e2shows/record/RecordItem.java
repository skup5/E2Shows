package cz.skup5.e2shows.record;

import android.graphics.Bitmap;

import org.jetbrains.annotations.NotNull;

import cz.skup5.e2shows.playlist.PlaylistItem;
import cz.skup5.jEvropa2.data.Item;

/**
 * Wrapper of Item record.
 *
 * @author Skup5
 */
public class RecordItem implements Comparable<RecordItem>, PlaylistItem {
    private Item record;
    private Bitmap cover = null;
    private RecordType type;

    public RecordItem(Item record, RecordType type) {
        this.record = record;
        this.type = type;
    }

    public Bitmap getCover() {
        return cover;
    }

    public Item getRecord() {
        return record;
    }

    public RecordType getType() {
        return type;
    }

    public boolean hasCover() {
        return cover != null;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    @Override
    public int compareTo(RecordItem recordItem) {
//    System.out.println(this);
//    System.out.println("RecordItem.compareTo");
//    System.out.println(recordItem);
//    System.out.println("----------------------------");
        int timeCmp = compareTimes(recordItem);
        if (timeCmp == 0)
            return getRecord().getName().compareTo(recordItem.getRecord().getName());
        else return timeCmp;
    }

    @Override
    public String toString() {
        return getRecord().getName();
    }

    private int compareTimes(RecordItem recordItem) {
        return (int) (recordItem.getRecord().getTimestamp() - getRecord().getTimestamp());
    }

    @NotNull
    @Override
    public String getTitle() {
        return getRecord().getName();
    }

    @NotNull
    @Override
    public String getArtist() {
        // FixMe: return show name
        return "";
    }
}
