package de.dentrassi.vat.nfc.programmer.nfc.ops;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import de.dentrassi.vat.nfc.programmer.nfc.AuthenticationFailedException;
import de.dentrassi.vat.nfc.programmer.nfc.Block;
import de.dentrassi.vat.nfc.programmer.nfc.Directory;
import de.dentrassi.vat.nfc.programmer.nfc.Key;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.SectorTrailer;

/**
 * Erase the MAD.
 * <p>
 * We erase the MAD by clearing out the sector trailer, resetting it to it's default and writing
 * an empty MAD.
 */
public class EraseMad extends BaseOperation<Void> {

    public static final String TAG = Eraser.class.getName();

    public EraseMad(@NonNull final MifareClassic card, @NonNull final Keys keys) {
        super(card, keys, 0);
    }

    @Override
    public Void perform() throws Exception {
        eraseAccess();
        eraseData();

        return null;
    }

    private void eraseAccess() throws Exception {
        final byte[] data = SectorTrailer.defaultTrailer().encode();

        try {
            // try key B first
            Log.i(TAG, "Try writing with key B");
            writeWithKey(this.keys.getB(), WithKey.B, Block.Block3, data);
        } catch (final AuthenticationFailedException e) {
            // try the unprovisioned card case next
            try {
                writeWithKey(Key.defaultKey(), WithKey.A, Block.Block3, data);
            } catch (final AuthenticationFailedException e2) {
                // try the unprovisioned card case next
                writeWithKey(Key.defaultKey(), WithKey.B, Block.Block3, data);
            }
        }
    }

    private void eraseData() throws Exception {
        Log.i(TAG, "eraseData");

        // start with an empty directory
        final Directory directory = Directory.empty();

        // render and write
        final byte[][] data = directory.toData();

        // as we erased the access keys, we can use the default key now
        writeWithKey(Key.defaultKey(), WithKey.A, Block.Block1, data);
    }
}
