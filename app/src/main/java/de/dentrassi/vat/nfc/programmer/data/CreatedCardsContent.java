package de.dentrassi.vat.nfc.programmer.data;

import androidx.annotation.NonNull;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import org.apache.commons.collections4.list.UnmodifiableList;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.dentrassi.vat.nfc.programmer.model.CardId;

public class CreatedCardsContent {

    private final List<CreatedCard> entries;
    private final Path path;

    public CreatedCardsContent(@NonNull final Path basePath) {
        this.entries = new ArrayList<>();

        // this must be aligned with `provider_paths.xml`
        this.path = basePath.resolve("export").resolve("cards.csv");
    }

    /**
     * Get an unmodifiable list of recorded cards.
     */
    public @NonNull List<CreatedCard> getEntries() {
        return UnmodifiableList.unmodifiableList(this.entries);
    }

    public @NonNull Path getPath() {
        return this.path;
    }

    /**
     * Clear the in-memory store.
     * <p>
     * <strong>NOTE:</strong> If changes should be stored, a call to {@link #store()} is required.
     */
    public void clear() {
        this.entries.clear();
    }

    /**
     * Add a new entry to the in-memory store.
     * <p>
     * <strong>NOTE:</strong> If changes should be stored, a call to {@link #store()} is required.
     *
     * @param entry the entry to add.
     */
    public void add(@NonNull final CreatedCard entry) {
        this.entries.add(entry);
    }


    /**
     * Remove card by tag UID.
     */
    public void remove(@NonNull final String uid) {
        this.entries.removeIf(card -> card.getUid().equals(uid));
    }

    /**
     * Loads the recorded cards.
     *
     * @throws Exception if anything goes wrong.
     */
    public void load() throws Exception {
        final List<CreatedCard> entries = new LinkedList<>();

        try (final Reader reader = Files.newBufferedReader(this.path, StandardCharsets.UTF_8);
             final CSVReader csv = new CSVReaderBuilder(reader)
                     .withSkipLines(1)
                     .build()) {

            String[] line;
            while ((line = csv.readNext()) != null) {
                final String uid = line[0];
                final CardId id = CardId.of(
                        Integer.parseInt(line[1], 10),
                        Integer.parseInt(line[2], 10),
                        UUID.fromString(line[3])
                );

                final ZonedDateTime timestamp = ZonedDateTime.parse(line[4]);
                entries.add(new CreatedCard(uid, id, timestamp));
            }
        }

        // replace, keeping the original list (as we might have handed it out)

        this.entries.clear();
        this.entries.addAll(entries);
    }

    /**
     * Store (persist) the current in-memory store.
     *
     * @throws IOException if anything goes wrong.
     */
    public void store() throws Exception {

        // ensure all parent directories exist

        Files.createDirectories(this.path.getParent());

        // write data

        try (final ICSVWriter csv = new CSVWriterBuilder(Files.newBufferedWriter(
                this.path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )).build()) {

            csv.writeNext(new String[]{
                    "Chip ID",
                    "Member ID",
                    "User Number",
                    "Card UID",
                    "Timestamp"
            });

            for (final CreatedCard card : this.entries) {
                csv.writeNext(new String[]{
                        card.getUid(),
                        Integer.toString(card.getId().getMemberId(), 10),
                        Integer.toString(card.getId().getCardNumber(), 10),
                        card.getId().getUid().toString(),
                        card.getTimestamp().toString()
                });
            }
        }
    }

}