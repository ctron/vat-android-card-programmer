package de.dentrassi.vat.nfc.programmer.nfc.action;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.io.BaseEncoding;

import java.util.function.BiConsumer;

import de.dentrassi.vat.nfc.programmer.data.CardId;
import de.dentrassi.vat.nfc.programmer.list.CreatedCard;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.Writer;

public class WriteAction extends TagAction<CreatedCard> {

    private static final String TAG = WriteAction.class.getName();

    private final Keys keys;
    private final CardId id;

    public WriteAction(@NonNull final Tag tag,
                       @NonNull final Keys keys,
                       @NonNull final CardId id,
                       @NonNull final BiConsumer<CreatedCard, Exception> outcome) {
        super(tag, outcome);
        this.id = id;
        this.keys = keys;
    }

    protected CreatedCard process() throws Exception {
        Log.i(TAG, "Start writing tag");

        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        new Writer(m, this.keys, this.id, 1)
                .perform();

        final String uid = BaseEncoding.base16().encode(m.getTag().getId());

        return new CreatedCard(uid, id);
    }

}
