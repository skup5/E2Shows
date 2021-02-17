package cz.skup5.e2shows.dao

import android.util.Log
import cz.skup5.e2shows.dto.ShowDto
import cz.skup5.e2shows.exception.ShowLoadingException
import cz.skup5.jEvropa2.dao.ShowDao.get
import cz.skup5.jEvropa2.data.Show
import kotlinx.coroutines.GlobalScope
import java.util.*

private const val LOGTAG = "OnlineShowDao"

/**
 * Implementation of [ShowDao] which downloading [Show] from Evropa2 server.
 *
 *
 * Created on 16.2.2017.
 *
 * @author Skup5
 */
class OnlineShowDao : ShowDao {

    override fun add(shows: List<ShowDto>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Throws(ShowLoadingException::class)
    override fun loadAll(): List<ShowDto> {
        Log.d(LOGTAG, "loadAll")
        val showList: MutableList<ShowDto> = ArrayList()
        val shows = get()
        if (shows.isEmpty()) {
            throw ShowLoadingException("Chyba při stahování Shows seznamu.")
        } else {
            for (show in shows) {
                showList.add(ShowDto(show))
            }
        }
        return showList
    }

    override suspend fun loadAllAsync(): List<ShowDto> {
        return GlobalScope.run { loadAll() }
    }
}