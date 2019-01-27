package cz.skup5.e2shows.downloader;

import android.content.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.skup5.e2shows.show.ShowItem;
import cz.skup5.jEvropa2.dao.ShowDao;
import cz.skup5.jEvropa2.data.Show;

/**
 * This downloader allows to download set of shows.
 * <p/>
 * Created on 5.1.2019
 *
 * @author Skup5
 */
public class ShowsDownloader extends AbstractDownloader<Void, Void, Set<ShowItem>> {

    public ShowsDownloader() {
        super();
    }

    public ShowsDownloader(Context context, String dialogTitle) {
        super(context, dialogTitle);
    }

    @Override
    protected Set<ShowItem> download(Void... voids) {
        final Set<ShowItem> showsSet = new HashSet<>();
        final List<Show> showList = ShowDao.INSTANCE.get();

        if (showList.isEmpty()) {
            errors.add("Chyba při stahování Shows seznamu.");
        } else {
            for (Show show : showList) {
                showsSet.add(new ShowItem(show));
            }
        }

        return showsSet;
    }
}
