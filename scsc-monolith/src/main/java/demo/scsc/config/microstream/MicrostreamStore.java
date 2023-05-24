package demo.scsc.config.microstream;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.types.StorageManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("SpellCheckingInspection")
public class MicrostreamStore {

    public static final Path BASE_STORAGE_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "SCSC");

    private static final Map<String, StorageManager> managers = new HashMap<>();


    public static synchronized StorageManager forLocation(String location) {
        StorageManager storageManager = managers.get(location);
        if (storageManager == null) {
            storageManager = EmbeddedStorage.start(BASE_STORAGE_PATH.resolve(location));
            managers.put(location, storageManager);
        }
        return storageManager;
    }
}
