package support

import constants.ApiErrorMessages
import dto.CreateUserErrorBody
import dto.UserListItemDto
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat

/**
 * Shared AssertJ helpers for HTTP responses (keeps tests/fixtures free of copy-pasted status/body checks).
 */
object CustomAssert {

    fun assertStatusOkWithBody(response: Response, vararg allowed: Int) {
        val set = allowed.toSet()
        assertThat(response.statusCode)
            .withFailMessage(
                "Expected HTTP status one of %s but got %s. Body: %s",
                set,
                response.statusCode,
                response.asString(),
            )
            .isIn(allowed.toList())
        assertThat(response.asString())
            .withFailMessage("Response body is empty. status=%s", response.statusCode)
            .isNotBlank
    }

    fun assertHttp400ForEmptyField(response: Response, field: String) {
        assertThat(response.statusCode)
            .withFailMessage(
                ApiErrorMessages.EMPTY_FIELD_EXPECTED_400,
                field,
                response.statusCode,
                response.asString(),
            )
            .isEqualTo(400)
    }

    /**
     * After rejecting a create with an empty required field: list must not contain a row matching
     * [username] or [email] (either can be blank when checking the other identity leg).
     */
    fun assertUsersListHasNoRowMatchingUsernameOrEmail(
        listResponse: Response,
        username: String,
        email: String,
    ) {
        assertStatusOkWithBody(listResponse, 200)
        val users = ResponseMapper.parseUserList(listResponse)
        val userExists = users.any { row ->
            row.username == username || row.email == email
        }
        assertThat(userExists)
            .describedAs(
                "Rejected empty field request must not create a user. username=%s, email=%s",
                username,
                email,
            )
            .isFalse()
    }

    fun assertDuplicateRejected(response: Response, field: String) {
        assertThat(response.statusCode)
            .withFailMessage(
                "Expected 400 Bad Request for duplicate %s, but got %s. Body: %s",
                field,
                response.statusCode,
                response.asString(),
            )
            .isEqualTo(400)
    }

    fun assertCreateUserErrorBody(response: Response, expectedMessagePart: String) {
        val errorBody = ResponseMapper.toDto<CreateUserErrorBody>(response)

        assertThat(errorBody.success)
            .withFailMessage(
                "Expected success=false in error response. Body: %s",
                response.asString(),
            )
            .isFalse()

        assertThat(errorBody.message.any { message ->
            message.contains(expectedMessagePart, ignoreCase = true)
        })
            .withFailMessage(
                "Expected error message to contain '%s'. Messages: %s. Body: %s",
                expectedMessagePart,
                errorBody.message,
                response.asString(),
            )
            .isTrue()
    }

    fun assertNoUserWithUsernameAndEmail(
        users: List<UserListItemDto>,
        username: String,
        email: String,
    ) {
        val userExists = users.any { user ->
            user.username == username && user.email == email
        }

        assertThat(userExists)
            .describedAs(
                "Rejected duplicate registration must not create a user. username=%s, email=%s",
                username,
                email,
            )
            .isFalse()
    }
}
