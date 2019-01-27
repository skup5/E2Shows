package cz.skup5.e2shows.manager;

import java.util.List;

import cz.skup5.e2shows.dto.ShowDto;
import cz.skup5.e2shows.exception.ShowLoadingException;

/**
 * Created on 16.2.2017.
 *
 * @author Skup5
 */
public interface ShowManager {

    /**
     * Loads and returns list of {@link ShowDto}
     *
     * @return list of shows or empty list
     * @throws ShowLoadingException
     */
    List<ShowDto> loadAllShows() throws ShowLoadingException;
}
