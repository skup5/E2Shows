package cz.skup5.e2shows.manager

import cz.skup5.e2shows.dto.ShowDto
import cz.skup5.e2shows.exception.ShowLoadingException
import cz.skup5.e2shows.listener.OnCompleteListener
import cz.skup5.e2shows.listener.OnErrorListener

/**
 * Created on 16.2.2017.
 *
 * @author Skup5
 */
interface ShowManager {
    /**
     * Loads and returns list of [ShowDto]
     *
     * @return list of shows or empty list
     */
    @Throws(ShowLoadingException::class)
    fun loadAllShows(): List<ShowDto?>?

    /**
     * It calls [ShowManager.loadAllShows] asynchronously.
     *
     * @param completeListener it is called when shows are loaded
     * @param errorListener    it is called if some error occurred while loading
     */
    fun loadAllShowsAsync(completeListener: OnCompleteListener<List<ShowDto>>, errorListener: OnErrorListener)
}