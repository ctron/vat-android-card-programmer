package de.dentrassi.vat.nfc.programmer.nfc.action;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.function.BiConsumer;

import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.ops.Eraser;
import de.dentrassi.vat.nfc.programmer.nfc.ops.WriteMad;

public class EraseAction extends TagAction<byte[]> {

    private static final String TAG = EraseAction.class.getName();

    private final Keys keys;

    public EraseAction(@NonNull final Tag tag,
                       @NonNull final Keys keys,
                       @NonNull final BiConsumer<byte[], Exception> outcome) {
        super(tag, outcome);
        this.keys = keys;
    }

    protected byte[] process() throws Exception {
        Log.i(TAG, "Start erasing tag");

        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        new WriteMad(m, this.keys, List.of()).perform();
        new Eraser(m, this.keys, 1)
                .perform();

        return m.getTag().getId();
    }

}
