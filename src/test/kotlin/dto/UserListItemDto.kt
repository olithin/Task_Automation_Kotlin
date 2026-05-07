package dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserListItemDto(
    val id: Int? = null,
    val username: String? = null,
    val email: String? = null,

    @JsonProperty("password")
    val passwordHash: String? = null,

    @JsonProperty("created_at")
    val createdAt: String? = null,

    @JsonProperty("updated_at")
    val updatedAt: String? = null,
)
