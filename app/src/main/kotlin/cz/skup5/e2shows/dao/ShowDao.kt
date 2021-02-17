package cz.skup5.e2shows.dao

import cz.skup5.e2shows.dto.ShowDto
import cz.skup5.e2shows.exception.ShowLoadingException

/**
 * Created on 16.2.2017.
 *
 * @author Skup5
 */
interface ShowDao {

    /**
     * Stores the given show list.
     */
    fun add(shows: List<ShowDto>)

    /**
     * Loads and returns list of [ShowDto] instances.
     *
     * @return list of shows or empty list
     */
    @Throws(ShowLoadingException::class)
    fun loadAll(): List<ShowDto>

    /**
     * Loads and returns list of [ShowDto] instances asynchronously via coroutine.
     */
    suspend fun loadAllAsync(): List<ShowDto>
}