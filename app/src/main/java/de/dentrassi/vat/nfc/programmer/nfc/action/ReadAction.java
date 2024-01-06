package de.dentrassi.vat.nfc.programmer.nfc.action;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Optional;
import java.util.function.BiConsumer;

import de.dentrassi.vat.nfc.programmer.codec.Plain;
import de.dentrassi.vat.nfc.programmer.model.CardId;
import de.dentrassi.vat.nfc.programmer.nfc.Block;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.Tools;

public class ReadAction extends TagAction<Optional<CardId>> {

    private final Keys keys;

    public ReadAction(@NonNull final Tag tag,
                      @NonNull final Keys keys,
                      @NonNull final BiConsumer<Optional<CardId>, Exception> outcome) {
        super(tag, outcome);
        this.keys = keys;
    }

    protected Optional<CardId> process() throws Exception {
        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        m.connect();
        try {

            if (!m.authenticateSectorWithKeyB(1, this.keys.getB().getKey())) {
                // FIXME: check for either case an provide a better message
                throw new Exception("Not authorized. Unprovisioned card or wrong key.");
            }

            final int blockIndex = Tools.blockIndexFrom(m, 1, Block.Block0);

            try {
                final byte[] data0 = m.readBlock(blockIndex);
                final byte[] data1 = m.readBlock(blockIndex + 1);
                return Optional.of(Plain.decode(data0, data1));
            } catch (final Exception e) {
                // FIXME: should report error
                Log.w("Failed to decode", e);
                return Optional.empty();
            }

        } finally {
            m.close();
        }
    }
}
