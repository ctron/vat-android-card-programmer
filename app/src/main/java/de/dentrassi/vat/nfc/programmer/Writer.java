package de.dentrassi.vat.nfc.programmer;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.io.BaseEncoding;

import java.util.function.BiConsumer;

import de.dentrassi.vat.nfc.programmer.data.CardId;
import de.dentrassi.vat.nfc.programmer.list.CreatedCard;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;

public class Writer extends TagAction<CreatedCard> {

    private static final String TAG = "Writer";

    private final CardId id;

    public Writer(@NonNull final Tag tag, @NonNull final CardId id, @NonNull final BiConsumer<CreatedCard, Exception> outcome) {
        super(tag, outcome);
        this.id = id;
    }

    protected CreatedCard process() throws Exception {
        Log.i(TAG, "Start writing tag");

        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        new de.dentrassi.vat.nfc.programmer.nfc.Writer(m, Keys.defaultKeys(), this.id, 1)
                .perform();

        final String uid = BaseEncoding.base16().encode(m.getTag().getId());

        return new CreatedCard(uid, id);
    }

}
