package de.dentrassi.vat.nfc.programmer.nfc.ops;

import static de.dentrassi.vat.nfc.programmer.nfc.AccessBits.BlockBits;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import de.dentrassi.vat.nfc.programmer.codec.Plain;
import de.dentrassi.vat.nfc.programmer.model.CardId;
import de.dentrassi.vat.nfc.programmer.nfc.AccessBits;
import de.dentrassi.vat.nfc.programmer.nfc.AuthenticationFailedException;
import de.dentrassi.vat.nfc.programmer.nfc.Block;
import de.dentrassi.vat.nfc.programmer.nfc.Key;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.SectorTrailer;

/**
 * Write information to card
 */
public class Writer extends BaseWriter {

    private static final String TAG = Writer.class.getName();

    private final @NonNull CardId id;

    public Writer(
            @NonNull final MifareClassic card,
            @NonNull final Keys keys,
            @NonNull final CardId id,
            final int sector
    ) {
        super(card, keys, sector);
        this.id = id;
    }

    @Override
    protected void write() throws Exception {
        writeAccess();
        writeId();
    }

    /**
     * Write the access control (sector trailer)
     */
    private void writeAccess() throws IOException {
        Log.i(TAG, "writeAccess");

        // Key A: read-only
        // Key B: read-write

        final AccessBits accessBits = new AccessBits();
        accessBits.setBlockBits(Block.Block0, new BlockBits(1, 0, 0));
        accessBits.setBlockBits(Block.Block1, new BlockBits(0, 0, 0));
        accessBits.setBlockBits(Block.Block2, new BlockBits(0, 0, 0));
        accessBits.setBlockBits(Block.Block3, new BlockBits(0, 1, 1));

        final byte[] data = SectorTrailer.of(this.keys, accessBits).encode();

        try {
            // try unprovisioned card first, using the default key as Key A
            writeWithKey(Key.defaultKey(), WithKey.A, Block.Block3, data);
        } catch (final AuthenticationFailedException e2) {
            // if that fails with an authentication error, try key B
            Log.i(TAG, "Try writing with key B");
            writeWithKey(this.keys.getB(), WithKey.B, Block.Block3, data);
        }
    }

    /**
     * Write the user ID to the card
     */
    private void writeId() throws Exception {
        Log.i(TAG, "writeId");

        final byte[][] data = Plain.encode(this.id);
        writeWithKey(this.keys.getB(), WithKey.B, Block.Block0, data);
    }

}
