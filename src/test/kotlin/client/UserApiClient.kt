package client

import data.NewUser
import io.restassured.RestAssured.given
import io.restassured.response.Response
import support.logger

class UserApiClient {

    private val log = logger<UserApiClient>()

    fun createUser(user: NewUser): Response {
        return createUser(
            username = user.username,
            email = user.email,
            password = user.password
        )
    }

    fun createUser(
        username: String?,
        email: String?,
        password: String?,
    ): Response {
        val request = given()

        if (username != null) {
            request.multiPart("username", username)
        }

        if (email != null) {
            request.multiPart("email", email)
        }

        if (password != null) {
            request.multiPart("password", password)
        }

        val response = request
            .post("/user/create")
            .then()
            .extract()
            .response()

        logHttpResponse("POST /user/create", response)
        return response
    }

    fun getUsers(): Response {
        val response = given()
            .get("/user/get")
            .then()
            .extract()
            .response()

        logHttpResponse("GET /user/get", response)
        return response
    }

    fun getUserById(userId: Int): Response {
        val response = given()
            .queryParam("id", userId)
            .get("/user/get")
            .then()
            .extract()
            .response()

        logHttpResponse("GET /user/get?id=$userId", response)
        return response
    }

    private fun logHttpResponse(action: String, response: Response) {
        log.debug(
            "{} status={} body={}",
            action,
            response.statusCode,
            response.asString(),
        )
    }
}
