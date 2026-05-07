package tests.business

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import support.CustomAssert
import support.ResponseMapper
import tests.BaseApiTest

/**
 * Verifies that a successfully created user is persisted by the system
 */
@Tag("p0")
@Tag("business")
class UserPersistenceTests : BaseApiTest() {


    @Test
    @Tag("TC-002")
    @DisplayName("TC-002: Created user is saved and visible in users list")
    fun shouldShowCreatedUserInUsersList() {
        // Arrange
        val registered = userFixtures.createRegisteredUser()

        // Act
        val response = api.getUsers()

        // Assert
        CustomAssert.assertStatusOkWithBody(response, 200)

        val users = ResponseMapper.parseUserList(response)

        val actualUser = users.find { user ->
            user.id == registered.id
        } ?: error("Created user was not found in users list. id=${registered.id}")

        assertThat(actualUser.username)
            .describedAs("Created username should be visible in users list")
            .isEqualTo(registered.payload.username)

        assertThat(actualUser.email)
            .describedAs("Created email should be visible in users list")
            .isEqualTo(registered.payload.email)
    }
}
