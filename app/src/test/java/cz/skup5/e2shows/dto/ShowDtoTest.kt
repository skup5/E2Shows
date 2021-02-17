 package cz.skup5.e2shows.dto

import cz.skup5.e2shows.utils.deserializeFromJson
import cz.skup5.e2shows.utils.serializeToJson
import cz.skup5.jEvropa2.data.Show
import org.junit.Test
import java.net.URI
import kotlin.test.DefaultAsserter


data class User(val name: String, val age: Int)

internal class ShowDtoTest {


    @Test
    fun decodeEncodeSerialization() {
        val showDto = ShowDto(Show(
                name = "Test show",
                slug = "test slug",
                webSiteUri = URI("https://evropa2.cz")
        ))

        val showDtoJson = serializeToJson(showDto)
        DefaultAsserter.assertEquals(
                null,
                """{"show":{"id":-1,"name":"Test show","webSiteUri":"https://evropa2.cz","slug":"test slug"},"recordItems":[],"audioPage":0,"nextPageUrl":null}""",
                showDtoJson
        )

        val showDtoObject: ShowDto = deserializeFromJson(showDtoJson, ShowDto::class.java)
        DefaultAsserter.assertEquals(null, showDto, showDtoObject)
    }

    @Test
    fun simpleSerialization() {
        val user = User("Aerith Gainsborough", 25)

        val userJson = serializeToJson(user)
        DefaultAsserter.assertEquals(null, """{"name":"Aerith Gainsborough","age":25}""", userJson)

        val userObject: User = deserializeFromJson(userJson, User::class.java)
        DefaultAsserter.assertEquals(null, user, userObject)
    }

}