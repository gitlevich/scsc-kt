package demo.scsc.config.microstream

import one.microstream.storage.embedded.types.EmbeddedStorage
import one.microstream.storage.types.StorageManager
import java.nio.file.Paths

@Suppress("SpellCheckingInspection")
object MicrostreamStore {
    val BASE_STORAGE_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "SCSC")
    private val managers: MutableMap<String, StorageManager?> = HashMap()
    @JvmStatic
    @Synchronized
    fun forLocation(location: String): StorageManager? {
        var storageManager = managers[location]
        if (storageManager == null) {
            storageManager = EmbeddedStorage.start(BASE_STORAGE_PATH.resolve(location))
            managers[location] = storageManager
        }
        return storageManager
    }
}
