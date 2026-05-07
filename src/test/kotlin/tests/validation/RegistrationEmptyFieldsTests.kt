package tests.validation

import constants.Const
import data.TestUserFactory
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import support.CustomAssert
import tests.BaseApiTest

/**
 * Required fields must not be accepted when sent as empty FormData values.
 */
@Tag("p1")
@Tag("validation")
class RegistrationEmptyFieldsTests : BaseApiTest() {

    @Test
    @Tag("TC-008")
    @DisplayName("TC-008: Reject registration with empty username")
    fun cannotRegisterWithEmptyUsername() {
        // Arrange
        val user = TestUserFactory.createValidUser()

        // Act
        val response = api.createUser(
            username = "",
            email = user.email,
            password = user.password,
        )

        // Assert
        CustomAssert.assertHttp400ForEmptyField(response, Const.USERNAME)
        CustomAssert.assertUsersListHasNoRowMatchingUsernameOrEmail(
            api.getUsers(),
            username = "",
            email = user.email,
        )
    }

    @Test
    @Tag("TC-008")
    @DisplayName("TC-008: Reject registration with empty email")
    fun cannotRegisterWithEmptyEmail() {
        // Arrange
        val user = TestUserFactory.createValidUser()

        // Act
        val response = api.createUser(
            username = user.username,
            email = "",
            password = user.password,
        )

        // Assert
        CustomAssert.assertHttp400ForEmptyField(response, Const.EMAIL)
        CustomAssert.assertUsersListHasNoRowMatchingUsernameOrEmail(
            api.getUsers(),
            username = user.username,
            email = "",
        )
    }

    @Test
    @Tag("TC-008")
    @DisplayName("TC-008: Reject registration with empty password")
    fun cannotRegisterWithEmptyPassword() {
        // Arrange
        val user = TestUserFactory.createValidUser()

        // Act
        val response = api.createUser(
            username = user.username,
            email = user.email,
            password = "",
        )

        // Assert
        CustomAssert.assertHttp400ForEmptyField(response, Const.PASSWORD)
        CustomAssert.assertUsersListHasNoRowMatchingUsernameOrEmail(
            api.getUsers(),
            username = user.username,
            email = user.email,
        )
    }
}
