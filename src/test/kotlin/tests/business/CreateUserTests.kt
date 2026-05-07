package tests.business

import data.TestUserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import tests.BaseApiTest

/**
 * P0 smoke tests covering the happy path of the user lifecycle.
 * These tests are independent and use unique data per run.
 */
class CreateUserTests : BaseApiTest() {

    @Test
    @Tag("p0")
    @Tag("TC-001")
    @DisplayName("POST /user/create registers user with valid FormData")
    fun shouldCreateUserWithValidData() {
        // Arrange: factory generates UUID-derived unique username and email.
        val user = TestUserFactory.createValidUser()

        // Act
        val response = api.createUser(user)

        // Assert: 201 Created is the REST-appropriate success code for resource creation.
        assertThat(response.statusCode)
            .describedAs("POST /user/create should return successful creation status")
            .isIn(201)
    }
}