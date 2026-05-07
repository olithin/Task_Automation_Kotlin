package data

import data.TestUserFactory.createValidUser
import java.util.*

/**
 * Factory for generating unique TestUser instances.
 *
 * Goals:
 *  - Avoid collisions between parallel runs and CI re-runs.
 *  - Keep usernames within typical length and charset constraints
 *    (alphanumeric + short uuid suffix, lowercase).
 *  - Use a strong default password so password-policy validations
 *    (if any) do not become flaky.
 *
 * No logging: log in tests where you need visibility.
 */
object TestUserFactory {

    private const val DEFAULT_PASSWORD = "Password123!"

    /**
     * Returns a fully unique valid user (username + email).
     */
    fun createValidUser(): NewUser {
        val unique = uniqueSuffix()
        return NewUser(
            username = "qa_user_$unique",
            email = "qa_user_$unique@example.com",
            password = DEFAULT_PASSWORD,
        )
    }

    /**
     * Fixed username and email (same default password as [createValidUser]).
     */
    fun createUser(username: String, email: String): NewUser =
        NewUser(username = username, email = email, password = DEFAULT_PASSWORD)

    /**
     * Unique user pinned to a specific email.
     * Useful for the duplicate-email scenario.
     */
    fun createUserWithEmail(email: String): NewUser {
        val unique = uniqueSuffix()
        return NewUser(
            username = "qa_user_$unique",
            email = email,
            password = DEFAULT_PASSWORD,
        )
    }

    /**
     * Unique user pinned to a specific password.
     * Useful for security tests that need a known unique password value
     * to search for in a GET response body.
     */
    fun createUserWithPassword(password: String): NewUser {
        val unique = uniqueSuffix()
        return NewUser(
            username = "qa_user_$unique",
            email = "qa_user_$unique@example.com",
            password = password,
        )
    }

    /**
     * Valid username and password but a syntactically invalid email value (no `@`),
     * unique per call so shared environments do not hit "email already exists" instead of format rules.
     */
    fun invalidEmailUser(): NewUser {
        val unique = uniqueSuffix()
        return NewUser(
            username = "qa_user_$unique",
            email = "invalid-email-without-at-$unique",
            password = DEFAULT_PASSWORD,
        )
    }

    private fun uniqueSuffix(): String =
        UUID.randomUUID().toString().take(12)
}
