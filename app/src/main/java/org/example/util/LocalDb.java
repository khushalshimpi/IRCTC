package org.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class LocalDb {
    private static final String DEFAULT_DATA_DIR_NAME = "localDb";

    private LocalDb() {}

    public static Path usersFile() throws IOException {
        return seededFile("users.json");
    }

    public static Path trainsFile() throws IOException {
        return seededFile("trains.json");
    }

    private static Path seededFile(String resourceName) throws IOException {
        Path dataDir = resolveDataDir();
        Files.createDirectories(dataDir);

        Path dataFile = dataDir.resolve(resourceName);
        if (Files.exists(dataFile)) {
            return dataFile;
        }

        try (InputStream inputStream = LocalDb.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IOException("Missing classpath resource: " + resourceName);
            }
            Files.copy(inputStream, dataFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return dataFile;
    }

    private static Path resolveDataDir() {
        String override = System.getenv("IRCTC_DB_DIR");
        if (override != null && !override.isBlank()) {
            return Paths.get(override.trim());
        }

        Path local = Paths.get(System.getProperty("user.dir")).resolve(DEFAULT_DATA_DIR_NAME);
        if (Files.isWritable(local.getParent())) {
            return local;
        }

        return Paths.get(System.getProperty("user.home")).resolve(".irctc");
    }
}
