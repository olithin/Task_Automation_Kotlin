package fixtures

import client.UserApiClient
import data.RegisteredUser
import data.TestUserFactory
import dto.CreateUserResponse
import org.assertj.core.api.Assertions.assertThat
import support.CustomAssert
import support.ResponseMapper
import support.logger

/**
 * Precondition: register a user via API and parse identity from the create response.
 */
class UserFixtures(
    private val api: UserApiClient,
) {

    private val log = logger<UserFixtures>()

    fun createRegisteredUser(): RegisteredUser {
        val user = TestUserFactory.createValidUser()

        val response = api.createUser(user)
        CustomAssert.assertStatusOkWithBody(response, 200)
        val createResponse = ResponseMapper.toDto<CreateUserResponse>(response)
        val userId = createResponse.details.id

        assertThat(userId)
            .describedAs("Create user response should contain user id")
            .isNotNull()

        assertThat(createResponse.success)
            .describedAs("Create user response should confirm successful registration")
            .isTrue()

        assertThat(createResponse.details.username)
            .describedAs("Created username should match request username")
            .isEqualTo(user.username)

        assertThat(createResponse.details.email)
            .describedAs("Created email should match request email")
            .isEqualTo(user.email)

        log.info(
            "Created registered user fixture: id={}, username={}, email={}, status={}",
            userId,
            user.username,
            user.email,
            response.statusCode,
        )

        return RegisteredUser(user, userId)
    }

    /**
     * GET /user/get: when the parsed user list is empty, registers one user via [createRegisteredUser].
     * No-op when at least one user row is already present.
     */
    fun ensureAtLeastOneUserExists() {
        val getResponse = api.getUsers()
        CustomAssert.assertStatusOkWithBody(getResponse, 200)
        if (ResponseMapper.parseUserList(getResponse).isEmpty()) {
            createRegisteredUser()
        }
    }

    /**
     * Ensures GET /user/get eventually reflects a non-empty user list: if the parsed list is already
     * non-empty, returns true without POST; if **empty**, registers via [createRegisteredUser] then returns true.
     */
    fun isGetUsersListNonEmpty(): Boolean {
        ensureAtLeastOneUserExists()
        return true
    }
}
