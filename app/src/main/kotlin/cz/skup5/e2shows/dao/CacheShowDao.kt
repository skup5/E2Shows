package cz.skup5.e2shows.dao

import android.content.Context
import cz.skup5.e2shows.cache.Cache
import cz.skup5.e2shows.cache.InternalCache
import cz.skup5.e2shows.dto.ShowDto
import cz.skup5.jEvropa2.data.Show
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

private const val CACHE_KEY_SHOWS = "shows"

/**
 * Implementation of [ShowDao] which represents [Show] cache and persisting data in a device.
 *
 *
 * Created on 16.2.2017.
 *
 * @author Skup5
 */
class CacheShowDao(context: Context) : ShowDao {

    private val cache: Cache<String, String>

    init {
        cache = InternalCache(context, "showPrefsCache")
    }

    override fun add(shows: List<ShowDto>) {
        cache.set(CACHE_KEY_SHOWS, transform(shows))
    }

    override fun loadAll(): List<ShowDto> {
        return runBlocking(Dispatchers.IO) { loadAllAsync() }
    }

    override suspend fun loadAllAsync(): List<ShowDto> {
        return cache.get(CACHE_KEY_SHOWS).await()?.run { inverseTransform(this) } ?: emptyList()
    }

    private fun inverseTransform(value: String): List<ShowDto> {
//            Json.parseList(value)
        TODO()
    }

    private fun transform(shows: List<ShowDto>): String {
//            Json.stringify(shows)
        TODO()
    }
}