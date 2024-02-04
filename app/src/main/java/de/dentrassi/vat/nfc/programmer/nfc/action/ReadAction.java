package de.dentrassi.vat.nfc.programmer.nfc.action;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;

import androidx.annotation.NonNull;

import java.util.function.BiConsumer;

import de.dentrassi.vat.nfc.programmer.model.CardId;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.ops.Reader;

public class ReadAction extends TagAction<CardId> {

    public static class UnauthorizedToReadException extends Exception {
        public UnauthorizedToReadException() {
            super("Not authorized. Unprovisioned card or wrong key.");
        }
    }

    private final Keys keys;

    public ReadAction(@NonNull final Tag tag,
                      @NonNull final Keys keys,
                      @NonNull final BiConsumer<CardId, Exception> outcome) {
        super(tag, outcome);
        this.keys = keys;
    }

    protected CardId process() throws Exception {
        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        m.connect();
        try (m) {
            return new Reader(m, this.keys, 1)
                    .perform();
        }
    }
}
