package de.dentrassi.vat.nfc.programmer.nfc.action;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.function.BiConsumer;

import de.dentrassi.vat.nfc.programmer.model.Uid;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.ops.EraseMad;
import de.dentrassi.vat.nfc.programmer.nfc.ops.Eraser;

public class EraseAction extends TagAction<Uid> {

    private static final String TAG = EraseAction.class.getName();

    private final Keys keys;

    public EraseAction(@NonNull final Tag tag,
                       @NonNull final Keys keys,
                       @NonNull final BiConsumer<Uid, Exception> outcome) {
        super(tag, outcome);
        this.keys = keys;
    }

    protected Uid process() throws Exception {
        Log.i(TAG, "Start erasing tag");

        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        m.connect();
        try (m) {
            new EraseMad(m, this.keys).perform();
            new Eraser(m, this.keys, 1)
                    .perform();

            return Uid.of(m.getTag().getId());
        }

    }

}
