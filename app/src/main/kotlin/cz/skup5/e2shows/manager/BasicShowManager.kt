package cz.skup5.e2shows.manager

import android.os.AsyncTask
import cz.skup5.e2shows.dao.OnlineShowDao
import cz.skup5.e2shows.dao.ShowDao
import cz.skup5.e2shows.dto.ShowDto
import cz.skup5.e2shows.exception.ShowLoadingException
import cz.skup5.e2shows.listener.OnCompleteListener
import cz.skup5.e2shows.listener.OnErrorListener
import java.util.*

/**
 * Basic implementation of [ShowManager] using Singleton pattern.
 *
 *
 * Created on 16.2.2017.
 *
 * @author Skup5
 */
object BasicShowManager : ShowManager {

    private val showDao: ShowDao = OnlineShowDao()

    @Throws(ShowLoadingException::class)
    override fun loadAllShows(): List<ShowDto> {
        return showDao.loadAll()
    }

    override fun loadAllShowsAsync(completeListener: OnCompleteListener<List<ShowDto>>, errorListener: OnErrorListener) {
        Loader(completeListener, errorListener).execute()
    }

    private class Loader(onCompleteListener: OnCompleteListener<List<ShowDto>>, onErrorListener: OnErrorListener) : AsyncTask<Void?, Void?, List<ShowDto>>() {
        val onCompleteListener: OnCompleteListener<List<ShowDto>>?
        val onErrorListener: OnErrorListener?
        val errors: MutableList<String>
        override fun doInBackground(vararg params: Void?): List<ShowDto> {
            try {
                return showDao.loadAll()
            } catch (e: ShowLoadingException) {
                errors.add(e.localizedMessage ?: e.message ?: "Unknown error.")
            }
            return emptyList()
        }

        override fun onPostExecute(showDtos: List<ShowDto>) {
            super.onPostExecute(showDtos)
            if (onErrorListener != null && errors.isNotEmpty()) {
                onErrorListener.onError(errors)
            }
            onCompleteListener?.onComplete(showDtos)
        }

        init {
            this.onCompleteListener = onCompleteListener
            this.onErrorListener = onErrorListener
            errors = ArrayList()
        }
    }

}