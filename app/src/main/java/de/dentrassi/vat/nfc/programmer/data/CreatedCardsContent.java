package de.dentrassi.vat.nfc.programmer.data;

import androidx.annotation.NonNull;

import com.google.common.io.BaseEncoding;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.dentrassi.vat.nfc.programmer.model.AdditionalInformation;
import de.dentrassi.vat.nfc.programmer.model.CardId;
import de.dentrassi.vat.nfc.programmer.model.IdType;

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
        remove(entry.getId().getUid());
        this.entries.add(0, entry);
    }


    /**
     * Remove card by tag UID.
     */
    public void remove(@NonNull final byte[] uid) {
        this.entries.removeIf(card -> Arrays.equals(card.getId().getUid(), uid));
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
                final CardId id = CardId.of(
                        Integer.parseInt(line[1], 10),
                        BaseEncoding.base16().decode(line[0])
                );
                final String name = line[2];
                final String identification = line[3];
                final IdType identificationType = IdType.fromString(line[4]);

                final AdditionalInformation additional = AdditionalInformation.of(name, identification, identificationType);

                final ZonedDateTime timestamp = ZonedDateTime.parse(line[5]);
                entries.add(CreatedCard.of(id, additional, timestamp));
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
                    "UID",
                    "Member ID",
                    "Name",
                    "Identification",
                    "Identification Type",
                    "Timestamp"
            });

            for (final CreatedCard card : this.entries) {
                csv.writeNext(new String[]{
                        BaseEncoding.base16().encode(card.getId().getUid()),
                        Integer.toString(card.getId().getMemberId(), 10),
                        card.getAdditional().getName(),
                        card.getAdditional().getId(),
                        card.getAdditional().getIdType().toString(),
                        card.getTimestamp().toString()
                });
            }
        }
    }

}