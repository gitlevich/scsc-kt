package demo.scsc.config.microstream

import one.microstream.storage.embedded.types.EmbeddedStorage
import one.microstream.storage.types.StorageManager
import java.nio.file.Paths

@Suppress("SpellCheckingInspection")
object MicrostreamStore {
    private val BASE_STORAGE_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "SCSC")
    private val managers: MutableMap<String, StorageManager> = mutableMapOf()

    @Synchronized
    fun forLocation(location: String): StorageManager =
        managers[location] ?: EmbeddedStorage.start(BASE_STORAGE_PATH.resolve(location))
            .also { managers[location] = it }
}
