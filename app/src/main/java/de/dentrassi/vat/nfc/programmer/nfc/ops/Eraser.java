package de.dentrassi.vat.nfc.programmer.nfc.ops;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import de.dentrassi.vat.nfc.programmer.nfc.AuthenticationFailedException;
import de.dentrassi.vat.nfc.programmer.nfc.Block;
import de.dentrassi.vat.nfc.programmer.nfc.Key;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.SectorTrailer;

public class Eraser extends BaseOperation<Void> {

    private static final String TAG = Eraser.class.getName();

    private static final byte[] EMPTY_BLOCK = new byte[16];

    public Eraser(
            @NonNull final MifareClassic card,
            @NonNull final Keys keys,
            final int sector
    ) {
        super(card, keys, sector);
    }

    @Override
    public Void perform() throws Exception {
        eraseId();
        eraseAccess();

        return null;
    }

    private void eraseAccess() throws IOException {
        Log.i(TAG, "eraseAccess");

        // erase the sector trailer

        final byte[] data = SectorTrailer.defaultTrailer().encode();

        try {
            // try with B key first
            writeWithKey(this.keys.getB(), WithKey.B, Block.Block3, data);
        } catch (final AuthenticationFailedException ignored) {
            // next, try with default key
            writeWithKey(Key.defaultKey(), WithKey.A, Block.Block3, data);
        }
    }

    private void eraseId() throws IOException {
        Log.i(TAG, "eraseId");

        // erase the first three blocks

        final byte[][] data = new byte[][]{EMPTY_BLOCK, EMPTY_BLOCK, EMPTY_BLOCK};

        try {
            // try with key B first
            writeWithKey(this.keys.getB(), WithKey.B, Block.Block0, data);
        } catch (final AuthenticationFailedException ignored) {
            // next, try with default key
            writeWithKey(Key.defaultKey(), WithKey.A, Block.Block0, data);
        }
    }
}
