package cz.skup5.e2shows.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import cz.skup5.e2shows.record.RecordItem
import cz.skup5.e2shows.record.RecordType
import cz.skup5.jEvropa2.data.Show
import java.net.URL
import java.util.*

/**
 * Wrapper of Show.
 *
 *
 * Created on 16.6.2016.
 *
 * @author Skup5
 */
data class ShowDto(        val show: Show) {

     private val recordItems: NavigableSet<RecordItem> = TreeSet()

    var audioPage = 0
        private set

    var nextPageUrl = EMPTY_URL

    fun addRecordItems(newRecordItems: Collection<RecordItem>) {
        recordItems += newRecordItems
    }

    /**
     * Returns set of [RecordItem]s with the requested [RecordType].
     *
     * @param type the requested [RecordType]. If is [RecordType.All], returns all record items
     * @return set of record items with the given [RecordType] or empty set
     */
    fun getRecordItemsWithType(type: RecordType): NavigableSet<RecordItem> {
        val filteredSet: NavigableSet<RecordItem> = TreeSet()
        if (type == RecordType.All) {
            filteredSet += recordItems
        } else {
            for (item in recordItems) {
                if (item.type == type) {
                    filteredSet += item
                }
            }
        }
        return filteredSet
    }

    /**
     * Increases the current audio page number and returns new value.
     *
     * @return increased value
     */
    fun incAudioPage(): Int {
        audioPage++
        return audioPage
    }

    /**
     * Decreases the current audio page number and returns new value.
     *
     * @return decreased value
     */
    fun decAudioPage(): Int {
        if (audioPage > 0) audioPage--
        return audioPage
    }

    /**
     * @return true if has none record items
     */
    @get: JsonIgnore
    val isEmpty: Boolean
        get() = recordItems.isEmpty()

    @get: JsonIgnore
    val info: String
        get() = show.info()

    @get: JsonIgnore
    val name: String
        get() = show.name

    fun hasNextPageUrl(): Boolean {
        return nextPageUrl !== EMPTY_URL
    }

    companion object {
        const val ITEMS_PER_PAGE = 10
        private val EMPTY_URL: URL? = null
    }

}