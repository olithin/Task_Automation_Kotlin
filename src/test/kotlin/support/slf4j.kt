package support

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * SLF4J logger named after [T] (standard class logger name). One place for the `getLogger(Class)` call;
 * each type still gets its own logger category for filtering.
 */
inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)

/** Same as [logger] but for a runtime class (e.g. [javaClass] on a test subclass). */
fun logger(clazz: Class<*>): Logger = LoggerFactory.getLogger(clazz)
