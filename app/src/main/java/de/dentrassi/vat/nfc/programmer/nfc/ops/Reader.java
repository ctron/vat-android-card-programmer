package de.dentrassi.vat.nfc.programmer.nfc.ops;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;

import de.dentrassi.vat.nfc.programmer.codec.Plain;
import de.dentrassi.vat.nfc.programmer.model.CardId;
import de.dentrassi.vat.nfc.programmer.nfc.Block;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.Tools;
import de.dentrassi.vat.nfc.programmer.nfc.action.ReadAction;

public class Reader extends BaseOperation<CardId> {

    private static final String TAG = Reader.class.getName();

    public static class RecordValidationFailed extends Exception {
        private final byte[] card;
        private final byte[] tag;

        public RecordValidationFailed(byte[] card, byte[] tag) {
            super("Record validation failed");
            this.card = card;
            this.tag = tag;
        }

        public byte[] getCard() {
            return this.card;
        }

        public byte[] getTag() {
            return this.tag;
        }
    }

    public Reader(
            @NonNull final MifareClassic card,
            @NonNull final Keys keys,
            final int sector
    ) {
        super(card, keys, sector);
    }

    @Override
    public CardId perform() throws Exception {

        if (!this.card.authenticateSectorWithKeyB(this.sector, this.keys.getB().getKey())) {
            Log.d(TAG, "Failed to authenticate reader");
            throw new ReadAction.UnauthorizedToReadException();
        }

        final int blockIndex = Tools.blockIndexFrom(this.card, this.sector, Block.Block0);

        final byte[] data = this.card.readBlock(blockIndex);
        Log.d(TAG, String.format("Read %s bytes", data.length));

        final CardId id = Plain.decode(data);

        if (id.isEmpty()) {
            return null;
        }

        var tagId = this.card.getTag().getId();

        if (!id.validateUid(tagId)) {
            Log.d(TAG, "Tag:  " + Arrays.toString(tagId));
            Log.d(TAG, "Card: " + Arrays.toString(id.getUid()));
            throw new RecordValidationFailed(id.getUid(), tagId);
        }

        return id;
    }
}
