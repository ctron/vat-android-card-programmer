package de.dentrassi.vat.nfc.programmer.data;

import androidx.annotation.NonNull;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.dentrassi.vat.nfc.programmer.model.AdditionalInformation;
import de.dentrassi.vat.nfc.programmer.model.CardId;
import de.dentrassi.vat.nfc.programmer.model.IdType;
import de.dentrassi.vat.nfc.programmer.model.Uid;

public class CreatedCardsContent {

    private final List<CardEntry> entries;

    public CreatedCardsContent() {
        this.entries = new ArrayList<>();
    }

    /**
     * Get an unmodifiable list of recorded cards.
     * <p>
     * Newest entries come last
     */
    public @NonNull List<CardEntry> getEntries() {
        return Collections.unmodifiableList(Lists.reverse(this.entries));
    }

    /**
     * Clear the in-memory store.
     * <p>
     * <strong>NOTE:</strong> If changes should be stored, a call to {@link #store(Writer)} is required.
     */
    public void clear() {
        this.entries.clear();
    }

    /**
     * Add a new entry to the in-memory store.
     * <p>
     * <strong>NOTE:</strong> If changes should be stored, a call to {@link #store(Writer)} is required.
     *
     * @param entry the entry to add.
     */
    public void add(@NonNull final CardEntry entry) {
        this.entries.removeIf(card -> card.getId().getUid().equals(entry.getId().getUid()));
        this.entries.add(entry);
    }


    /**
     * Erase card by tag UID.
     * <p>
     * We don't remove the card from the list, but mark it as erased
     */
    public void erase(@NonNull final Uid uid) {
        this.entries.removeIf(card -> card.getId().getUid().equals(uid));
        this.entries.add(CardEntry.ofErased(uid, ZonedDateTime.now()));
    }


    /**
     * Loads the recorded cards.
     *
     * @throws Exception if anything goes wrong.
     */
    public void load(@NonNull final Reader reader) throws Exception {
        final LinkedList<CardEntry> entries = new LinkedList<>();

        try (final CSVReader csv = new CSVReaderBuilder(reader)
                .withSkipLines(1)
                .build()) {

            String[] line;
            while ((line = csv.readNext()) != null) {
                final CardId id = CardId.of(
                        Integer.parseInt(line[2], 10),
                        BaseEncoding.base16().decode(line[0])
                );
                final boolean erased = Boolean.parseBoolean(line[1]);
                final String name = line[3];
                final String identification = line[4];
                final IdType identificationType = IdType.fromString(line[5]);

                final AdditionalInformation additional = AdditionalInformation.of(name, identification, identificationType);

                final ZonedDateTime timestamp = ZonedDateTime.parse(line[6]);
                entries.add(CardEntry.ofStored(id, erased, additional, timestamp));
            }
        }

        // replace, keeping the original list (as we might have handed out a reference)

        replace(entries);
    }

    private void replace(@NonNull final List<CardEntry> entries) {
        clear();
        this.entries.addAll(entries);
    }

    /**
     * Loads the recorded cards.
     *
     * @throws Exception if anything goes wrong.
     */
    public void load(@NonNull final CreatedCardStorage storage) throws Exception {
        try (final Reader reader = storage.createReader()) {
            load(reader);
        }
    }

    /**
     * Store (persist) the current in-memory store.
     *
     * @throws IOException if anything goes wrong.
     */
    public void store(@NonNull final Writer writer) throws Exception {

        // write data

        try (final ICSVWriter csv = new CSVWriterBuilder(writer).build()) {

            csv.writeNext(new String[]{
                    "UID", // 0
                    "Erased", // 1
                    "Member ID", // 2
                    "Name", // 3
                    "Identification", // 4
                    "Identification Type", // 5
                    "Timestamp" // 6
            });

            for (final CardEntry card : this.entries) {
                csv.writeNext(new String[]{
                        BaseEncoding.base16().encode(card.getId().getUid().getUid()),
                        Boolean.toString(card.isErased()),
                        Integer.toString(card.getId().getMemberId(), 10),
                        card.getAdditional().getName(),
                        card.getAdditional().getId(),
                        card.getAdditional().getIdType().toString(),
                        card.getTimestamp().toString()
                });
            }
        }
    }

    /**
     * Store (persist) the current in-memory store.
     *
     * @throws IOException if anything goes wrong.
     */
    public void store(@NonNull final CreatedCardStorage storage) throws Exception {
        try (final Writer writer = storage.createWriter()) {
            store(writer);
        }
    }

}