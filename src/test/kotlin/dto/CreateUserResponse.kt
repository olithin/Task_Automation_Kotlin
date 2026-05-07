package dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Wire format for POST /user/create success body.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateUserResponse(
    val success: Boolean,
    val details: CreateUserDetails,
    val message: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateUserDetails(
    val id: Int,
    val username: String,
    val email: String,

    @JsonProperty("password")
    val passwordHash: String,

    @JsonProperty("created_at")
    val createdAt: String,

    @JsonProperty("updated_at")
    val updatedAt: String,
)
