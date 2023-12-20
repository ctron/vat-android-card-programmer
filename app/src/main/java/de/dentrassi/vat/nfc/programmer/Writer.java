package de.dentrassi.vat.nfc.programmer;

import android.app.Activity;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.function.BiConsumer;

import de.dentrassi.vat.nfc.programmer.data.CardId;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;

public class Writer extends TagAction<Void> {

    private static final String TAG = "Writer";

    private final CardId id;

    public Writer(@NonNull final Activity activity, @NonNull final Tag tag, @NonNull final CardId id, @NonNull final BiConsumer<Void, Exception> outcome) {
        super(tag, activity, outcome);
        this.id = id;
    }

    protected Void process() throws Exception {
        Log.i(TAG, "Start writing tag");

        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        new de.dentrassi.vat.nfc.programmer.nfc.Writer(m, Keys.defaultKeys(), this.id, 1)
                .perform();

        return null;
    }

}
