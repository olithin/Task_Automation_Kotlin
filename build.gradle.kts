plugins {
    kotlin("jvm") version "2.3.21"
}

group = "com.example.api.tests"
version = "1.0.0"

repositories {
    mavenCentral()
}

// Centralised version constants. Kept simple on purpose — for a 9-test suite
// a libs.versions.toml catalog would be overkill.
object V {
    const val REST_ASSURED = "5.5.2"
    const val ASSERTJ = "3.27.3"
    // JUnit 5 (Jupiter). Pinned to the 5.x line on purpose: the JUnit Platform
    // shipped by common IDE-embedded test runners (e.g. IntelliJ) is 1.x, which is
    // compatible with Jupiter 5.x. JUnit 6 requires Platform 6.x and is not
    // yet supported by every IDE-side runner.
    const val JUNIT = "5.14.4"
    const val JACKSON_KOTLIN = "2.21.3"
    const val SLF4J = "2.0.17"
    const val LOGBACK = "1.5.18"
}

dependencies {
    // Kotlin std lib
    implementation(kotlin("stdlib"))

    // RestAssured for HTTP requests (multipart + json-path)
    testImplementation("io.rest-assured:rest-assured:${V.REST_ASSURED}")
    testImplementation("io.rest-assured:json-path:${V.REST_ASSURED}")

    // JUnit 5 via the BOM. The BOM aligns Jupiter 5.14.x with Platform 1.14.x
    // automatically, so we never have to manage two version numbers manually.
    testImplementation(platform("org.junit:junit-bom:${V.JUNIT}"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.assertj:assertj-core:${V.ASSERTJ}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    // Required by Gradle 9.x to launch the JUnit Platform from the test task.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Jackson for flexible JSON parsing of unknown response shapes.
    // Pinned to the 2.x line on purpose: Jackson 3 renames packages
    // (com.fasterxml.jackson.* -> tools.jackson.*) which would force a code
    // change without any benefit for this project.
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:${V.JACKSON_KOTLIN}")

    // Logging: plain SLF4J API + Logback as the single SLF4J binding.
    // Logback also serves as the SLF4J provider for RestAssured / Jackson.
    testImplementation("org.slf4j:slf4j-api:${V.SLF4J}")
    testRuntimeOnly("ch.qos.logback:logback-classic:${V.LOGBACK}")
}

// Use Java 21 to match the GitHub Actions runner (api-tests.yml).
kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()

    // Forward TestConfig-related system properties from the Gradle JVM into
    // the test JVM. Without this, `-DbaseUrl=...` on the command line is
    // visible to Gradle but not to the test workers it forks.
    listOf("baseUrl", "connectTimeoutMs", "socketTimeoutMs").forEach { key ->
        System.getProperty(key)?.let { systemProperty(key, it) }
    }

    testLogging {
        events("passed", "skipped", "failed")
        // Surface logback output in the Gradle console.
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    // Keep HTML and JUnit XML reports enabled by default (CI consumes both).
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
}
