package cz.skup5.e2shows.dao

import cz.skup5.e2shows.dto.ShowDto
import cz.skup5.e2shows.exception.ShowLoadingException
import cz.skup5.jEvropa2.dao.ShowDao.get
import cz.skup5.jEvropa2.data.Show
import java.util.*

/**
 * Implementation of [ShowDao] which downloading [Show] from Evropa2 server.
 *
 *
 * Created on 16.2.2017.
 *
 * @author Skup5
 */
class OnlineShowDao : ShowDao {

    @Throws(ShowLoadingException::class)
    override fun loadAll(): List<ShowDto> {
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
}