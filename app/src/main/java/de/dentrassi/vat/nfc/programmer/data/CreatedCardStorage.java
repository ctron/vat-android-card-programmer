package de.dentrassi.vat.nfc.programmer.data;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CreatedCardStorage {
    private final Path path;

    public CreatedCardStorage(@NonNull final Path basePath) {
        // this must be aligned with `provider_paths.xml`
        this.path = basePath.resolve("export").resolve("cards.csv");
    }

    public Writer createWriter() throws IOException {
        // ensure all parent directories exist
        Files.createDirectories(this.path.getParent());

        // now open for writing
        return Files.newBufferedWriter(
                this.path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    public Reader createReader() throws IOException {
        return Files.newBufferedReader(this.path, StandardCharsets.UTF_8);
    }

    public Path getPath() {
        return this.path;
    }
}
