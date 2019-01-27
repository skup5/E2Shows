package cz.skup5.e2shows.dao;

import java.util.ArrayList;
import java.util.List;

import cz.skup5.e2shows.dto.ShowDto;
import cz.skup5.e2shows.exception.ShowLoadingException;
import cz.skup5.jEvropa2.data.Show;

/**
 * Implementation of {@link ShowDao} which downloading {@link Show} from Evropa2 server.
 * <p>
 * Created on 16.2.2017.
 *
 * @author Skup5
 */
public class OnlineShowDao implements ShowDao {

    public OnlineShowDao() {
    }

    @Override
    public List<ShowDto> loadAll() throws ShowLoadingException {
        final List<ShowDto> showList = new ArrayList<>();
        final List<Show> shows = cz.skup5.jEvropa2.dao.ShowDao.INSTANCE.get();

        if (shows.isEmpty()) {
            throw new ShowLoadingException("Chyba při stahování Shows seznamu.");
        } else {
            for (Show show : shows) {
                showList.add(new ShowDto(show));
            }
        }

        return showList;
    }

}
