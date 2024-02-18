package de.dentrassi.vat.nfc.programmer.data;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.stream.Collectors.toList;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.List;

import de.dentrassi.vat.nfc.programmer.model.AdditionalInformation;
import de.dentrassi.vat.nfc.programmer.model.CardId;
import de.dentrassi.vat.nfc.programmer.model.Uid;

public class CreatedCardsContentTest {

    static final Uid TAG_1 = Uid.of(new byte[]{0, 0, 0, 1});
    static final Uid TAG_2 = Uid.of(new byte[]{0, 0, 0, 2});
    static final Uid TAG_3 = Uid.of(new byte[]{0, 0, 0, 3});
    static final Uid TAG_4 = Uid.of(new byte[]{0, 0, 0, 4});
    static final Uid TAG_5 = Uid.of(new byte[]{0, 0, 0, 5});


    @Test
    public void testOrder() {
        var content = new CreatedCardsContent();
        var entries = content.getEntries();

        // perform some actions

        content.add(CardEntry.ofCreated(CardId.of(1, TAG_3), AdditionalInformation.ofDefault(), ZonedDateTime.now()));
        content.add(CardEntry.ofCreated(CardId.of(2, TAG_1), AdditionalInformation.ofDefault(), ZonedDateTime.now()));
        content.add(CardEntry.ofCreated(CardId.of(1, TAG_1), AdditionalInformation.ofDefault(), ZonedDateTime.now()));
        content.add(CardEntry.ofCreated(CardId.of(2, TAG_2), AdditionalInformation.ofDefault(), ZonedDateTime.now()));
        content.add(CardEntry.ofCreated(CardId.of(4, TAG_4), AdditionalInformation.ofDefault(), ZonedDateTime.now()));
        content.add(CardEntry.ofCreated(CardId.of(5, TAG_5), AdditionalInformation.ofDefault(), ZonedDateTime.now()));
        content.add(CardEntry.ofCreated(CardId.of(3, TAG_3), AdditionalInformation.ofDefault(), ZonedDateTime.now()));
        content.add(CardEntry.ofErased(TAG_5, ZonedDateTime.now()));
        content.add(CardEntry.ofErased(TAG_4, ZonedDateTime.now()));


        // the same entries instance must now contain he data

        assertThat(entries).isNotEmpty();

        // assert the order of the inserted

        assertThat(entries.stream()
                .filter(card -> !card.isErased())
                .map(CardEntry::getId).collect(toList()))
                .containsExactly(
                        CardId.of(3, TAG_3),
                        CardId.of(2, TAG_2),
                        CardId.of(1, TAG_1)
                );

        // assert the order of the erased

        assertThat(entries.stream()
                .filter(CardEntry::isErased)
                .map(CardEntry::getId).collect(toList()))
                .containsExactly(
                        CardId.of(0, TAG_4),
                        CardId.of(0, TAG_5)
                );

        // assert the total order

        assertThat(entries.stream()
                .map(CardEntry::getId).collect(toList()))
                .containsExactly(
                        CardId.of(0, TAG_4),
                        CardId.of(0, TAG_5),
                        CardId.of(3, TAG_3),
                        CardId.of(2, TAG_2),
                        CardId.of(1, TAG_1)
                );
    }

    @Test
    public void testStoreAndLoad() throws Exception {

        // create a list of events

        var entries = List.of(
                CardEntry.ofCreated(CardId.of(1, TAG_1), AdditionalInformation.ofDefault(), ZonedDateTime.now()),
                CardEntry.ofCreated(CardId.of(2, TAG_2), AdditionalInformation.ofDefault(), ZonedDateTime.now()),
                CardEntry.ofErased(TAG_3, ZonedDateTime.now()),
                CardEntry.ofCreated(CardId.of(4, TAG_4), AdditionalInformation.ofDefault(), ZonedDateTime.now())
        );

        // create a reversed version for asserting

        var reversedEntries = entries.toArray(CardEntry[]::new);
        ArrayUtils.reverse(reversedEntries);

        // create the store and add those events

        var content = new CreatedCardsContent();
        for (var entry : entries) {
            content.add(entry);
        }

        // assert the order

        assertThat(content.getEntries())
                .containsExactly(reversedEntries);

        // serialize to CSV

        final String data;
        try (final StringWriter sw = new StringWriter()) {
            content.store(sw);
            data = sw.toString();
        }

        // re-create the store from the serialized CSV

        content = new CreatedCardsContent();
        try (final StringReader sr = new StringReader(data)) {
            content.load(sr);
        }

        // assert that we get the same output

        assertThat(content.getEntries())
                .containsExactly(reversedEntries);
    }
}
