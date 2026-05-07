package dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Wire format for POST /user/create when the API returns a JSON body with [success] false and no user [details].
 * Example: `{ "success": false, "message": [ "Email already exists" ] }`.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateUserErrorBody(
    val success: Boolean,
    val message: List<String> = emptyList(),
)
