package tests.validation

import constants.ApiErrorMessages
import constants.Const
import data.TestUserFactory
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import tests.BaseApiTest

/**
 * Required multipart fields for POST /user/create: username, email, password.
 */
@Tag("p0")
@Tag("validation")
class RegistrationRequiredFieldsTests : BaseApiTest() {

    @Test
    @Tag("TC-005")
    @DisplayName("User cannot be registered without username (field omitted)")
    fun cannotRegisterWithoutUsername() {
        val data = TestUserFactory.createValidUser()
        val response = api.createUser(username = null, email = data.email, password = data.password)
        assertValidationError(response, Const.USERNAME)
    }

    @Test
    @Tag("TC-005")
    @DisplayName("User cannot be registered without email (field omitted)")
    fun cannotRegisterWithoutEmail() {
        val data = TestUserFactory.createValidUser()
        val response = api.createUser(username = data.username, email = null, password = data.password)
        assertValidationError(response, Const.EMAIL)
    }

    @Test
    @Tag("TC-005")
    @DisplayName("User cannot be registered without password (field omitted)")
    fun cannotRegisterWithoutPassword() {
        val data = TestUserFactory.createValidUser()
        val response = api.createUser(username = data.username, email = data.email, password = null)
        assertValidationError(response, Const.PASSWORD)
    }

    private fun assertValidationError(response: Response, field: String) {
        assertThat(response.statusCode)
            .withFailMessage(
                ApiErrorMessages.MISSING_FIELD_EXPECTED_400,
                field,
                response.statusCode,
                response.asString(),
            )
            .isEqualTo(400)
    }
}
