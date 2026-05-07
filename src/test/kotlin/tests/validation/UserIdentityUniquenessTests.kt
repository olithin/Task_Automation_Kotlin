package tests.validation

import constants.ApiErrorMessages
import data.TestUserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import support.CustomAssert
import support.ResponseMapper
import tests.BaseApiTest
import java.util.UUID

/**
 * API must reject a second account that reuses an existing email or an existing username
 */
@Tag("p0")
@Tag("validation")
class UserIdentityUniquenessTests : BaseApiTest() {

    @Test
    @Tag("TC-004")
    @DisplayName("TC-004: Reject second registration with the same email")
    fun shouldRejectDuplicateEmail() {
        // Arrange
        val salt = UUID.randomUUID().toString().take(8)
        val email = "shared.email.$salt@example.com"

        val firstUser = TestUserFactory.createUser("user_a_$salt", email)
        val secondUser = TestUserFactory.createUser("user_b_$salt", email)

        assertThat(secondUser.username)
            .describedAs("Test setup should use different usernames")
            .isNotEqualTo(firstUser.username)

        api.createUser(firstUser).also { firstResponse ->
            CustomAssert.assertStatusOkWithBody(firstResponse, 200, 201)
        }

        // Act
        val secondResponse = api.createUser(secondUser)

        // Assert
        CustomAssert.assertDuplicateRejected(secondResponse, field = "email")

        CustomAssert.assertCreateUserErrorBody(
            secondResponse,
            expectedMessagePart = ApiErrorMessages.EMAIL_ALREADY_EXISTS,
        )

        assertUserWasNotCreated(secondUser.username, secondUser.email)
    }

    @Test
    @Tag("TC-019")
    @DisplayName("TC-019: Reject second registration with the same username")
    fun shouldRejectDuplicateUsername() {
        // Arrange
        val salt = UUID.randomUUID().toString().take(8)
        val username = "shared_user_$salt"

        val firstUser = TestUserFactory.createUser(username, "a.$salt@example.com")
        val secondUser = TestUserFactory.createUser(username, "b.$salt@example.com")

        assertThat(secondUser.email)
            .describedAs("Test setup should use different emails")
            .isNotEqualTo(firstUser.email)

        api.createUser(firstUser).also { firstResponse ->
            CustomAssert.assertStatusOkWithBody(firstResponse, 200, 201)
        }

        // Act
        val secondResponse = api.createUser(secondUser)

        // Assert
        CustomAssert.assertDuplicateRejected(secondResponse, "username")
        CustomAssert.assertCreateUserErrorBody(
            secondResponse,
            ApiErrorMessages.USERNAME_ALREADY_TAKEN,
        )
        assertUserWasNotCreated(secondUser.username, secondUser.email)
    }

    private fun assertUserWasNotCreated(username: String, email: String) {
        val response = api.getUsers()
        CustomAssert.assertStatusOkWithBody(response, 200)
        CustomAssert.assertNoUserWithUsernameAndEmail(
            ResponseMapper.parseUserList(response),
            username,
            email,
        )
    }
}
