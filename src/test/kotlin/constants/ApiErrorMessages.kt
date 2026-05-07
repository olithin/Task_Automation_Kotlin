package constants

/**
 * Error-related strings for tests: wire texts from POST /user/create (`message` JSON array)
 * and shared AssertJ templates for required-field scenarios (TC-005 omitted, TC-008 empty).
 */
object ApiErrorMessages {
    const val EMAIL_ALREADY_EXISTS = "Email already exists"
    const val USERNAME_ALREADY_TAKEN = "This username is taken. Try another."

    /** AssertJ `withFailMessage` when a required multipart part is omitted but status ≠ 400 (TC-005). Not an API body string. */
    const val MISSING_FIELD_EXPECTED_400 = "Expected 400 for missing %s, but got %s. Body: %s"

    /** AssertJ `withFailMessage` when a required multipart part is empty string but status ≠ 400 (TC-008). Not an API body string. */
    const val EMPTY_FIELD_EXPECTED_400 = "Expected 400 for empty %s, but got %s. Body: %s"
}
