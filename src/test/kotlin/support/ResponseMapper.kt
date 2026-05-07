package support

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dto.UserListItemDto
import io.restassured.response.Response

object ResponseMapper {
    val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    inline fun <reified T> toDto(response: Response): T {
        return mapper.readValue(response.asString())
    }

    /**
     * GET /user/get returns a root JSON array of users.
     */
    fun parseUserList(response: Response): List<UserListItemDto> {
        val body = response.asString().trim()

        if (body.isBlank()) {
            return emptyList()
        }

        return mapper.readValue(body)
    }
}
