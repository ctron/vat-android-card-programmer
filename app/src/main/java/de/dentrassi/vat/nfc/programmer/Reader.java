package de.dentrassi.vat.nfc.programmer;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;

import java.util.Optional;
import java.util.function.BiConsumer;

import de.dentrassi.vat.nfc.programmer.codec.Plain;
import de.dentrassi.vat.nfc.programmer.data.CardId;
import de.dentrassi.vat.nfc.programmer.nfc.Tools;

public class Reader extends TagAction<Optional<CardId>> {

    private static final String TAG = "Reader";

    public Reader(final Tag tag, final BiConsumer<Optional<CardId>, Exception> outcome) {
        super(tag, outcome);
    }

    protected Optional<CardId> process() throws Exception {
        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        m.connect();
        try {

            m.authenticateSectorWithKeyA(1, MifareClassic.KEY_NFC_FORUM);

            final int blockIndex = Tools.blockIndexFrom(m, 1, 0);

            try {
                final byte[] data = m.readBlock(blockIndex);
                return Optional.of(Plain.decode(data));
            } catch (Exception e) {
                return Optional.empty();
            }


        } finally {
            m.close();
        }
    }
}
