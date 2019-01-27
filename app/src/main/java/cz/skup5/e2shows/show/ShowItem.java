package cz.skup5.e2shows.show;

import cz.skup5.e2shows.record.RecordItem;
import cz.skup5.e2shows.record.RecordType;

import java.net.URL;
import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

import cz.skup5.jEvropa2.data.Show;

/**
 * Wrapper of Show.
 * <p/>
 * Created on 16.6.2016.
 *
 * @author Skup5
 */
public class ShowItem {

    public static final int ITEMS_PER_PAGE = 10;

    private static final URL EMPTY_URL = null;

    private final NavigableSet<RecordItem> recordItems = new TreeSet<>();
    //          audioRecords = new TreeSet<>(),
//          videoRecords = new TreeSet<>();
    private Show show;
    private int audioPage = 0;
    private URL nextPageUrl = EMPTY_URL;

    public ShowItem(Show show) {
        this.show = show;
    }

    public void addRecordItems(Collection<RecordItem> newRecordItems) {
//    for (RecordItem item : newRecordItems) {
//      recordItems.add(item);
//    }
//    for (RecordItem r : newRecordItems)
//      System.out.println(r);
//    System.out.println("##################");

        recordItems.addAll(newRecordItems);
    }

    /**
     * Returns set of {@link RecordItem}s with the requested {@link RecordType}.
     *
     * @param type the requested {@link RecordType}. If is {@link RecordType#All}, returns all record items
     * @return set of record items with the given {@link RecordType} or empty set
     */
    public NavigableSet<RecordItem> getRecordItemsWithType(RecordType type) {
        NavigableSet<RecordItem> filteredSet = new TreeSet<>();
        if (type == RecordType.All) {
            filteredSet.addAll(recordItems);
        } else {
            for (RecordItem item : recordItems) {
                if (item.getType() == type) {
                    filteredSet.add(item);
                }
            }
        }
        return filteredSet;
    }

    /**
     * Increases the current audio page number and returns new value.
     *
     * @return increased value
     */
    public int incAudioPage() {
        audioPage++;
        return audioPage;
    }

    /**
     * Decreases the current audio page number and returns new value.
     *
     * @return decreased value
     */
    public int decAudioPage() {
        if (audioPage > 0) audioPage--;
        return audioPage;
    }

    /**
     * @return true if has none record items
     */
    public boolean isEmpty() {
        return recordItems.isEmpty();
    }

    public int getAudioPage() {
        return audioPage;
    }

    public Show getShow() {
        return show;
    }

    public URL getNextPageUrl() {
        return nextPageUrl;
    }

    public boolean hasNextPageUrl() {
        return nextPageUrl != EMPTY_URL;
    }

    public void setNextPageUrl(URL nextPageUrl) {
        this.nextPageUrl = nextPageUrl;
    }
}
