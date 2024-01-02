package de.dentrassi.vat.nfc.programmer.list;

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
import java.util.ArrayList;
import java.util.List;

import de.dentrassi.vat.nfc.programmer.data.CardId;

public class CreatedCardsContent {

    private List<CreatedCard> entries;
    private final Path path;

    public CreatedCardsContent(final Path path) {
        this.entries = new ArrayList<>();
        this.path = path;
    }

    public List<CreatedCard> getEntries() {
        return UnmodifiableList.unmodifiableList(this.entries);
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
                entries.add(new CreatedCard(uid, id));
            }
        }

        this.entries = entries;
    }

    public void store() throws IOException {
        try (final ICSVWriter csv = new CSVWriterBuilder(Files.newBufferedWriter(
                this.path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )).build()) {

            csv.writeNext(new String[]{"UID", "Member Id", "Card Number"});

            for (final CreatedCard card : this.entries) {
                csv.writeNext(new String[]{
                        card.getUid(),
                        Integer.toString(card.getId().getMemberId(), 10),
                        Integer.toString(card.getId().getCardNumber(), 10),
                });
            }
        }
    }

}