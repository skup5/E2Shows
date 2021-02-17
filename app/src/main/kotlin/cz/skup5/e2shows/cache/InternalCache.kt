package cz.skup5.e2shows.cache

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File
import kotlin.collections.HashMap
import kotlin.collections.MutableMap
import kotlin.collections.set

private const val LOGTAG = "InternalCache"

/**
 * Created on 29.2.2020
 *
 * @author Roman Zelenik
 */
class InternalCache(private val context: Context, s: String) : Cache<String, String> {

    private val rootDir = context.cacheDir
    private val cacheFile = File(rootDir, s)
    private val data: MutableMap<String, String> = HashMap()

    override fun get(key: String): Deferred<String?> {
        return GlobalScope.async {
            Log.d(LOGTAG, "get[$key]")
            if (!data.containsKey(key)) {
                val dataFile = File(cacheFile, key)
                val readData = if (dataFile.exists()) dataFile.readText() else ""
                data[key] = readData
            }
            data[key]
        }
    }

    override fun set(key: String, value: String): Deferred<Unit> {
        return GlobalScope.async {
            Log.d(LOGTAG, "set[$key] = $value")
            with(context) {
                data[key] = value
                File(cacheFile, key).writeText(value)
            }
        }
    }
}