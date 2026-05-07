package tests.security

import data.TestUserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import support.CustomAssert
import tests.BaseApiTest

/**
 * Security checks for user-related API responses.
 */
@Tag("security")
class UserSecurityTests : BaseApiTest() {

    @Test
    @Tag("p0")
    @Tag("TC-006")
    @DisplayName("TC-006: Raw password is not exposed in create user response")
    fun rawPasswordIsNotExposedInUsersList() {
        // Arrange
        val user = TestUserFactory.createUserWithPassword("SecretPassword123!")

        // Act
        val response = api.createUser(user)

        // Assert
        CustomAssert.assertStatusOkWithBody(response, 200, 201)
        assertThat(response.asString())
            .describedAs("POST /user/create must not echo the raw password from the multipart request")
            .doesNotContain(user.password)
    }
}