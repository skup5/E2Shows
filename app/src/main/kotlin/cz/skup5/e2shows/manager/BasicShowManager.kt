package cz.skup5.e2shows.manager

import android.content.Context
import android.os.AsyncTask
import cz.skup5.e2shows.MainActivity
import cz.skup5.e2shows.dao.CacheShowDao
import cz.skup5.e2shows.dao.OnlineShowDao
import cz.skup5.e2shows.dao.ShowDao
import cz.skup5.e2shows.dto.ShowDto
import cz.skup5.e2shows.exception.ShowLoadingException
import cz.skup5.e2shows.listener.OnCompleteListener
import cz.skup5.e2shows.listener.OnErrorListener
import kotlinx.coroutines.runBlocking
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

    private val networkDao: ShowDao = OnlineShowDao()
    private val cacheDao: ShowDao = CacheShowDao(MainActivity.context!!)

    @Throws(ShowLoadingException::class)
    override fun loadAllShows(): List<ShowDto> {
        return runBlocking { loadAllShowsAsync() }
    }

    override fun loadAllShowsAsync(completeListener: OnCompleteListener<List<ShowDto>>, errorListener: OnErrorListener) {
        Loader(completeListener, errorListener).execute()
    }

    override suspend fun loadAllShowsAsync(): List<ShowDto> {
        var shows = cacheDao.loadAllAsync()
        if (shows.isEmpty()) {
            shows = networkDao.loadAllAsync()
            cacheDao.add(shows)
        }
        return shows
    }

    private class Loader(onCompleteListener: OnCompleteListener<List<ShowDto>>, onErrorListener: OnErrorListener) : AsyncTask<Void?, Void?, List<ShowDto>>() {
        val onCompleteListener: OnCompleteListener<List<ShowDto>>?
        val onErrorListener: OnErrorListener?
        val errors: MutableList<String>
        override fun doInBackground(vararg params: Void?): List<ShowDto> {
            try {
                runBlocking { loadAllShowsAsync() }
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