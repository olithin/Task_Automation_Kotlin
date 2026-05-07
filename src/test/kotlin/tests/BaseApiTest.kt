package tests

import client.UserApiClient
import config.TestConfig
import fixtures.UserFixtures

/**
 * Base class for API tests.
 */
abstract class BaseApiTest {

    protected val api: UserApiClient by lazy {
        TestConfig.configure()
        createApiClient()
    }

    protected val userFixtures: UserFixtures by lazy {
        UserFixtures(api)
    }

    protected open fun createApiClient(): UserApiClient = UserApiClient()
}
