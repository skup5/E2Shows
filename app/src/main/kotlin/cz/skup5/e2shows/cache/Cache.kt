package cz.skup5.e2shows.cache

import kotlinx.coroutines.Deferred

/**
 * Created on 29.2.2020
 *
 * @author Roman Zelenik
 */
interface Cache<Key : Any, Value : Any> {
    fun get(key: Key): Deferred<Value?>
    fun set(key: Key, value: Value): Deferred<Unit>
}