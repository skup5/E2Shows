package cz.skup5.e2shows.downloader;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.skup5.e2shows.record.RecordItem;
import cz.skup5.e2shows.record.RecordType;
import cz.skup5.jEvropa2.Extractor;
import cz.skup5.jEvropa2.dao.ItemDao;
import cz.skup5.jEvropa2.data.Item;
import cz.skup5.jEvropa2.data.Show;

/**
 * This downloader allows to download set of records.
 * <p/>
 * Created on 5.1.2019
 *
 * @author Skup5
 */
public class RecordsDownloader extends AbstractDownloader<Void, Void, Set<RecordItem>> {

    private final Show show;
    private final int page;
    private final int itemsPerPage;

    /**
     * @param page         the required page of items
     * @param itemsPerPage the number of items per one page
     * @param show         the specific show which items will be downloaded
     */
    public RecordsDownloader(int page, int itemsPerPage, @Nullable Show show) {
        super();
        this.show = show;
        this.page = page;
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * @param context
     * @param dialogTitle
     * @param page         the required page of items
     * @param itemsPerPage the number of items per one page
     * @param show         the specific show which items will be downloaded
     */
    public RecordsDownloader(Context context, String dialogTitle, int page, int itemsPerPage, @Nullable Show show) {
        super(context, dialogTitle);
        this.page = page;
        this.itemsPerPage = itemsPerPage;
        this.show = show;
    }

    @Override
    protected Set<RecordItem> download(Void... voids) {
        final Set<RecordItem> records = new HashSet<>();
        final List<Item> itemList = ItemDao.INSTANCE.get(show, page, itemsPerPage);
        for (Item i : itemList) {
            records.add(new RecordItem(i, RecordType.Audio));
        }
        return records;
    }
}
