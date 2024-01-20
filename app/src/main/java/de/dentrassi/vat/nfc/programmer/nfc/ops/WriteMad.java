package de.dentrassi.vat.nfc.programmer.nfc.ops;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import de.dentrassi.vat.nfc.programmer.nfc.AccessBits;
import de.dentrassi.vat.nfc.programmer.nfc.AuthenticationFailedException;
import de.dentrassi.vat.nfc.programmer.nfc.Block;
import de.dentrassi.vat.nfc.programmer.nfc.Directory;
import de.dentrassi.vat.nfc.programmer.nfc.Key;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.SectorTrailer;

/**
 * Write a simple MAD.
 * <p>
 * <strong>NOTE: </strong> This needs to be extended in a way that it actually reads/updates/writes
 * an existing MAD.
 */
public class WriteMad extends BaseWriter {
    private static final String TAG = WriteMad.class.getName();

    private final int applicationSector;
    private final short applicationId;

    public WriteMad(@NonNull final MifareClassic card, @NonNull final Keys keys, final int applicationSector, final short applicationId) {
        super(card, keys, 0);
        this.applicationSector = applicationSector;
        this.applicationId = applicationId;
    }

    @Override
    protected void write() throws Exception {
        writeAccess();
        writeData();
    }

    private void writeAccess() throws Exception {

        final AccessBits accessBits = new AccessBits();
        accessBits.setBlockBits(Block.Block0, new AccessBits.BlockBits());
        accessBits.setBlockBits(Block.Block1, new AccessBits.BlockBits(1, 0, 0));
        accessBits.setBlockBits(Block.Block2, new AccessBits.BlockBits(1, 0, 0));
        accessBits.setBlockBits(Block.Block3, new AccessBits.BlockBits(0, 1, 1));

        // the MAD type and version
        // bit 7: directory available
        // bit 6: multi application
        // bit 5..2: reserved
        // bit 1..0: version
        accessBits.setUserData((byte) 0b10000001);

        // Key A: the default MAD key
        // Key B: our read-write key

        final byte[] data = SectorTrailer.of(Keys.of(
                        Key.applicationDirectory(),
                        this.keys.getB()
                ), accessBits)
                .encode();

        try {
            // try unprovisioned card first, using the default key as Key A
            writeWithKey(Key.defaultKey(), WithKey.A, Block.Block3, data);
        } catch (final AuthenticationFailedException e2) {
            // if that fails with an authentication error, try key B
            Log.i(TAG, "Try writing with key B");
            writeWithKey(this.keys.getB(), WithKey.B, Block.Block3, data);
        }
    }

    private void writeData() throws Exception {
        Log.i(TAG, "writeData");

        final Directory directory = Directory.empty();
        directory.setInfoSector(1);
        directory.setApplication(this.applicationSector, this.applicationId);

        final byte[][] data = directory.toData();

        writeWithKey(this.keys.getB(), WithKey.B, Block.Block1, data);
    }
}
