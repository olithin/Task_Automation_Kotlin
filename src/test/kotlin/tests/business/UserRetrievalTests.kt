package tests.business

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import support.CustomAssert
import support.ResponseMapper
import tests.BaseApiTest

/**
 * Retrieval contract: after registration, an API consumer can read users back via GET /user/get.
 */
@Tag("p0")
@Tag("business")
class UserRetrievalTests : BaseApiTest() {

    @Test
    @Tag("TC-003")
    @DisplayName("API consumers must be able to retrieve registered users")
    fun consumersMustBeAbleToRetrieveRegisteredUsers() {
        // Arrange
        userFixtures.ensureAtLeastOneUserExists()

        // Act
        val response = api.getUsers()

        // Assert
        CustomAssert.assertStatusOkWithBody(response, 200)

        val users = ResponseMapper.parseUserList(response)

        assertThat(users)
            .describedAs("GET /user/get should return at least one registered user")
            .isNotEmpty()

        users.forEach { user ->
            assertThat(user.id)
                .describedAs("User id should be assigned by server")
                .isPositive()

            assertThat(user.username)
                .describedAs("Username should not be blank")
                .isNotBlank()

            assertThat(user.email)
                .describedAs("Email should not be blank")
                .isNotBlank()
        }
    }

    @Test
    @Tag("TC-018")
    @DisplayName("TC-018: GET /user/get?id=id returns single row for created user")
    fun shouldReturnUserById() {
        // Arrange
        val registered = userFixtures.createRegisteredUser()

        // Act
        val response = api.getUserById(registered.id)

        // Assert
        CustomAssert.assertStatusOkWithBody(response, 200)

        val returnedUsers = ResponseMapper.parseUserList(response)

        assertThat(returnedUsers)
            .describedAs("GET /user/get?id should return exactly one user")
            .hasSize(1)

        val returnedUser = returnedUsers.first()

        assertThat(returnedUser.id)
            .describedAs("Returned user id should match requested id")
            .isEqualTo(registered.id)

        assertThat(returnedUser.username)
            .describedAs("Returned username should match created username")
            .isEqualTo(registered.payload.username)

        assertThat(returnedUser.email)
            .describedAs("Returned email should match created email")
            .isEqualTo(registered.payload.email)
    }
}