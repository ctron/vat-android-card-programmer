package de.dentrassi.vat.nfc.programmer.nfc;

import static de.dentrassi.vat.nfc.programmer.nfc.AccessBits.BlockBits;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import de.dentrassi.vat.nfc.programmer.codec.Plain;
import de.dentrassi.vat.nfc.programmer.model.CardId;

/**
 * Write information to card
 */
public class Writer {

    private static final String TAG = Writer.class.getName();

    private final MifareClassic card;
    private final Keys keys;
    private final CardId id;
    private final int sector;

    public Writer(final MifareClassic card, final Keys keys, final CardId id, final int sector) {
        this.card = card;
        this.keys = keys;
        this.id = id;
        this.sector = sector;
    }

    public void perform() throws Exception {
        this.card.connect();
        try {
            writeAccess();
            writeId();
        } finally {
            this.card.close();
        }
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
            // try unprovisioned card first, using the default key
            writeWithKey(Key.defaultKey(), Block.Block3, data);
        } catch (final AuthenticationFailedException e) {
            // if that fails with an authentication error, try key B
            Log.i(TAG, "Try writing with key B");
            writeWithKey(this.keys.getB(), Block.Block3, data);
        }
    }

    /**
     * Write the user ID to the card
     */
    private void writeId() throws Exception {
        Log.i(TAG, "writeId");

        final byte[][] data = Plain.encode(this.id);
        writeWithKey(this.keys.getB(), Block.Block0, data);
    }

    /**
     * Perform a write operation with a provided key.
     *
     * @param key   The key to use.
     * @param block The block to write.
     * @param data  The data to write, must be 16 bytes.
     */
    private void writeWithKey(final Key key, @NonNull final Block block, @NonNull final byte[] data) throws IOException {
        writeWithKey(key, block, new byte[][]{data});
    }

    /**
     * Perform a write operation with a provided key.
     *
     * @param key        The key to use.
     * @param startBlock The block to start writing, incrementing by one.
     * @param blocks     The blocks to write, must be 16 bytes each.
     * @throws IllegalArgumentException if the write operation would write beyond the sector boundary.
     */
    private void writeWithKey(final Key key, @NonNull final Block startBlock, @NonNull final byte[][] blocks) throws IOException {

        int blockIndex = Tools.blockIndexFrom(this.card, this.sector, startBlock);

        Log.i(TAG, String.format("writeWithKey - blockIndex: %s, blocks: %s", blockIndex, blocks.length));

        // ensure sector boundary (post logging)

        if (startBlock.blockNumber() + blocks.length > 4) {
            throw new IllegalArgumentException("Trying to write beyond sector boundary");
        }

        // authenticate first

        try {
            boolean result = this.card.authenticateSectorWithKeyB(this.sector, key.getKey());
            if (!result) {
                throw new AuthenticationFailedException();
            }
        } catch (final IOException e) {
            Log.w(TAG, "Failed to authenticate", e);
            throw e;
        }

        // write blocks

        for (final byte[] data : blocks) {
            try {
                this.card.writeBlock(blockIndex, data);
            } catch (final IOException e) {
                Log.w(TAG, "Failed to write", e);
                throw e;
            }

            blockIndex += 1;
        }
    }

}
