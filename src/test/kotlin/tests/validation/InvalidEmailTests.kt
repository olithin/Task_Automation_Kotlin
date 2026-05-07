package tests.validation

import data.TestUserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import support.CustomAssert
import support.ResponseMapper
import tests.BaseApiTest

/**
 * TC-007: syntactically invalid email must be rejected and must not be persisted.
 */
@Tag("p1")
@Tag("validation")
class InvalidEmailTests : BaseApiTest() {

    @Test
    @Tag("TC-007")
    @DisplayName(
        "TC-007: Reject invalid email format",
    )
    fun invalidEmailIsRejectedAndNotStored() {
        // Arrange
        val user = TestUserFactory.invalidEmailUser()

        // Act
        val response = api.createUser(user)

        // Assert
        assertThat(response.statusCode)
            .withFailMessage(
                "Expected 4xx for invalid email format, but got %s. Body: %s",
                response.statusCode,
                response.asString(),
            )
            .isBetween(400, 499)

        val listResponse = api.getUsers()
        CustomAssert.assertStatusOkWithBody(listResponse, 200)

        val users = ResponseMapper.parseUserList(listResponse)

        assertThat(users.none { row -> row.username == user.username })
            .describedAs(
                "Rejected invalid email request must not create a user. username=%s, email=%s",
                user.username,
                user.email,
            )
            .isTrue()

    }
}
