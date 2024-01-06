package de.dentrassi.vat.nfc.programmer.nfc.action;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.io.BaseEncoding;

import java.util.function.BiConsumer;

import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.ops.Eraser;

public class EraseAction extends TagAction<String> {

    private static final String TAG = EraseAction.class.getName();

    private final Keys keys;

    public EraseAction(@NonNull final Tag tag,
                       @NonNull final Keys keys,
                       @NonNull final BiConsumer<String, Exception> outcome) {
        super(tag, outcome);
        this.keys = keys;
    }

    protected String process() throws Exception {
        Log.i(TAG, "Start erasing tag");

        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        new Eraser(m, this.keys, 1)
                .perform();

        return BaseEncoding.base16().encode(m.getTag().getId());
    }

}
