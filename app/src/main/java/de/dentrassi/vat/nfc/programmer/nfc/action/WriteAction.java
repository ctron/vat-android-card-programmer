package de.dentrassi.vat.nfc.programmer.nfc.action;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.util.function.BiConsumer;

import de.dentrassi.vat.nfc.programmer.data.CreatedCard;
import de.dentrassi.vat.nfc.programmer.model.CardId;
import de.dentrassi.vat.nfc.programmer.model.WriteCardInformation;
import de.dentrassi.vat.nfc.programmer.nfc.ApplicationIds;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.ops.WriteMad;
import de.dentrassi.vat.nfc.programmer.nfc.ops.Writer;

public class WriteAction extends TagAction<CreatedCard> {

    private static final String TAG = WriteAction.class.getName();

    private final Keys keys;
    private final WriteCardInformation information;

    public WriteAction(@NonNull final Tag tag,
                       @NonNull final Keys keys,
                       @NonNull final WriteCardInformation information,
                       @NonNull final BiConsumer<CreatedCard, Exception> outcome) {
        super(tag, outcome);
        this.information = information;
        this.keys = keys;
    }

    protected CreatedCard process() throws Exception {
        Log.i(TAG, "Start writing tag");

        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        final byte[] uid = m.getTag().getId();
        if (uid.length < 4 || uid.length > 7) {
            throw new IllegalStateException(String.format("Card has a UID length of %s, which is not supported. It must be between 4 and 7 bytes long.", uid.length));
        }

        final CardId id = CardId.of(this.information.getMemberId(), uid);

        new Writer(m, this.keys, id, 1)
                .perform();
        new WriteMad(m, this.keys, 1, ApplicationIds.VAT)
                .perform();

        final ZonedDateTime timestamp = ZonedDateTime.now();

        return CreatedCard.of(id, this.information.getAdditional(), timestamp);
    }

}
