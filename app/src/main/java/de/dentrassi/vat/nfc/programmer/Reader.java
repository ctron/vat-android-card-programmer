package de.dentrassi.vat.nfc.programmer;

import android.app.Activity;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.BiConsumer;

import de.dentrassi.vat.nfc.programmer.codec.Plain;
import de.dentrassi.vat.nfc.programmer.data.CardId;
import de.dentrassi.vat.nfc.programmer.nfc.Tools;

public class Reader extends TagAction<Optional<CardId>> {

    private static final String TAG = "Reader";

    private final Tag tag;

    public Reader(final Tag tag, final Activity activity, final BiConsumer<Optional<CardId>, Exception> outcome) {
        super(tag, activity, outcome);
        this.tag = tag;
    }

    protected Optional<CardId> process() throws Exception {
        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        m.connect();
        try {

            final ByteBuffer buf = ByteBuffer.allocate(16);
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
