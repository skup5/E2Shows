package cz.skup5.e2shows.manager;

import java.util.List;

import cz.skup5.e2shows.dto.ShowDto;
import cz.skup5.e2shows.exception.ShowLoadingException;
import cz.skup5.e2shows.listener.OnCompleteListener;
import cz.skup5.e2shows.listener.OnErrorListener;

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

    /**
     * It calls {@link ShowManager#loadAllShows()} asynchronously.
     *
     * @param completeListener it is called when shows are loaded
     * @param errorListener    it is called if some error occurred while loading
     */
    void loadAllShowsAsync(OnCompleteListener<List<ShowDto>> completeListener, OnErrorListener errorListener);
}
