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
import java.util.List;

import de.dentrassi.vat.nfc.programmer.model.CardId;

public class CreatedCardsContent {

    private List<CreatedCard> entries;
    private final Path path;

    public CreatedCardsContent(final Path basePath) {
        this.entries = new ArrayList<>();

        // this must be aligned to `provider_paths.xml`
        this.path = basePath.resolve("export").resolve("cards.csv");
    }

    public List<CreatedCard> getEntries() {
        return UnmodifiableList.unmodifiableList(this.entries);
    }

    public Path getPath() {
        return this.path;
    }

    @SuppressWarnings("unused")
    public void clear() {
        this.entries.clear();
    }

    public void add(@NonNull final CreatedCard entry) {
        this.entries.add(entry);
    }

    public void load() throws Exception {
        final List<CreatedCard> entries = new ArrayList<>();

        try (final Reader reader = Files.newBufferedReader(this.path, StandardCharsets.UTF_8);
             final CSVReader csv = new CSVReaderBuilder(reader)
                     .withSkipLines(1)
                     .build()) {

            String[] line;
            while ((line = csv.readNext()) != null) {
                final String uid = line[0];
                final CardId id = CardId.of(
                        Integer.parseInt(line[1], 10),
                        Integer.parseInt(line[2], 10)
                );

                final ZonedDateTime timestamp = ZonedDateTime.parse(line[3]);
                entries.add(new CreatedCard(uid, id, timestamp));
            }
        }

        this.entries = entries;
    }

    public void store() throws IOException {

        Files.createDirectories(this.path.getParent());

        try (final ICSVWriter csv = new CSVWriterBuilder(Files.newBufferedWriter(
                this.path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )).build()) {

            csv.writeNext(new String[]{
                    "UID",
                    "Member Id",
                    "Card Number",
                    "Timestamp"
            });

            for (final CreatedCard card : this.entries) {
                csv.writeNext(new String[]{
                        card.getUid(),
                        Integer.toString(card.getId().getMemberId(), 10),
                        Integer.toString(card.getId().getCardNumber(), 10),
                        card.getTimestamp().toString()
                });
            }
        }
    }

}