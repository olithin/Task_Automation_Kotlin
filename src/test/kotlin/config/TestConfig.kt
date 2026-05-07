package config

import io.restassured.RestAssured
import io.restassured.config.HttpClientConfig
import io.restassured.config.RestAssuredConfig
import java.util.*

/**
 * Central test configuration.
 *
 * Resolution order for every setting:
 *   1. JVM system property:    -D<key>=...
 *   2. Environment variable:   <ENV_KEY>
 *   3. src/test/resources/test.properties: <key>=...
 *
 * baseUrl is mandatory and fails fast if missing.
 * Timeouts have sane defaults but can be overridden via the same chain.
 *
 * configure() is idempotent and safe to call from any @BeforeAll.
 */
object TestConfig {

    private const val BASE_URL_PROP_KEY = "baseUrl"
    private const val BASE_URL_ENV_KEY = "BASE_URL"

    private const val CONNECT_TIMEOUT_PROP_KEY = "connectTimeoutMs"
    private const val CONNECT_TIMEOUT_ENV_KEY = "CONNECT_TIMEOUT_MS"

    private const val SOCKET_TIMEOUT_PROP_KEY = "socketTimeoutMs"
    private const val SOCKET_TIMEOUT_ENV_KEY = "SOCKET_TIMEOUT_MS"

    private const val PROPERTIES_PATH = "/test.properties"

    // Defaults are used only when no source defines the timeout.
    private const val DEFAULT_CONNECT_TIMEOUT_MS = 10_000
    private const val DEFAULT_SOCKET_TIMEOUT_MS = 30_000

    private val fileProperties: Properties by lazy { loadProperties() }

    val baseUrl: String by lazy {
        resolveString(BASE_URL_PROP_KEY, BASE_URL_ENV_KEY)
            ?: throw IllegalStateException(
                """
                |baseUrl is not configured. Set it via one of:
                |  1. JVM system property: -DbaseUrl=<url>
                |  2. environment variable: BASE_URL=<url>
                |  3. src/test/resources/test.properties: baseUrl=<url>
                """.trimMargin(),
            )
    }

    val connectTimeoutMs: Int by lazy {
        resolveInt(CONNECT_TIMEOUT_PROP_KEY, CONNECT_TIMEOUT_ENV_KEY)
            ?: DEFAULT_CONNECT_TIMEOUT_MS
    }

    val socketTimeoutMs: Int by lazy {
        resolveInt(SOCKET_TIMEOUT_PROP_KEY, SOCKET_TIMEOUT_ENV_KEY)
            ?: DEFAULT_SOCKET_TIMEOUT_MS
    }

    /**
     * Apply RestAssured global configuration: base URI and HTTP timeouts.
     * Idempotent — safe to call from any @BeforeAll.
     */
    fun configure() {
        RestAssured.baseURI = baseUrl
        RestAssured.basePath = ""

        RestAssured.config = RestAssuredConfig.config().httpClient(
            HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", connectTimeoutMs)
                .setParam("http.socket.timeout", socketTimeoutMs),
        )
    }

    /**
     * Apply the resolution chain for a string value.
     * Returns null only when no source provides a non-blank value.
     */
    private fun resolveString(propertyKey: String, envKey: String): String? {
        System.getProperty(propertyKey)?.takeIf { it.isNotBlank() }?.let { return it }
        System.getenv(envKey)?.takeIf { it.isNotBlank() }?.let { return it }
        return fileProperties.getProperty(propertyKey)?.takeIf { it.isNotBlank() }
    }

    /**
     * Same chain as resolveString but typed as Int.
     * Throws on a malformed value rather than silently falling back to default,
     * because a typo in a config file is a real bug and should fail fast.
     */
    private fun resolveInt(propertyKey: String, envKey: String): Int? {
        val raw = resolveString(propertyKey, envKey) ?: return null
        return raw.toIntOrNull()
            ?: throw IllegalStateException(
                "Configuration value for '$propertyKey' is not a valid integer: '$raw'",
            )
    }

    private fun loadProperties(): Properties {
        val props = Properties()
        TestConfig::class.java.getResourceAsStream(PROPERTIES_PATH)?.use { stream ->
            props.load(stream)
        }
        return props
    }
}
