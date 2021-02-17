/**
 * Created on 24.5.2020
 *
 * @author Roman Zelenik
 */
@file: JvmName("Serializer")
package cz.skup5.e2shows.utils

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.*
//import com.google.gson.Gson
import java.lang.reflect.Type


//private val gson  = Gson()
private val jackson = createObjectMapper()

private fun createObjectMapper(): ObjectMapper {
    val objectMapper = jacksonObjectMapper()
    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    return objectMapper
}

//fun serializeToJson(obj:Any): String = gson.toJson(obj)
fun serializeToJson(obj:Any): String = jackson.writeValueAsString(obj)

//fun <T> deserializeFromJson(json:String, type : Type):T = gson.fromJson(json, type)
fun <T> deserializeFromJson(json:String, type : Class<T>):T = jackson.readValue(json, type)