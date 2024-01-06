package de.dentrassi.vat.nfc.programmer.nfc.ops;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.io.BaseEncoding;

import java.io.IOException;

import de.dentrassi.vat.nfc.programmer.nfc.AuthenticationFailedException;
import de.dentrassi.vat.nfc.programmer.nfc.Block;
import de.dentrassi.vat.nfc.programmer.nfc.Key;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.Tools;

public abstract class BaseWriter {
    private static final String TAG = BaseWriter.class.getName();

    protected final @NonNull MifareClassic card;
    protected final @NonNull Keys keys;
    protected final int sector;

    protected enum WithKey {
        A, B
    }

    public BaseWriter(
            final @NonNull MifareClassic card,
            final @NonNull Keys keys,
            int sector
    ) {
        this.card = card;
        this.keys = keys;
        this.sector = sector;
    }

    protected abstract void write() throws Exception;

    public void perform() throws Exception {
        this.card.connect();
        try {
            write();
        } finally {
            this.card.close();
        }
    }

    /**
     * Perform a write operation with a provided key.
     *
     * @param key     The key to use.
     * @param withKey Use key as A or B.
     * @param block   The block to write.
     * @param data    The data to write, must be 16 bytes.
     */
    protected void writeWithKey(final Key key, @NonNull final WithKey withKey, @NonNull final Block block, @NonNull final byte[] data) throws IOException {
        writeWithKey(key, withKey, block, new byte[][]{data});
    }

    /**
     * Perform a write operation with a provided key.
     *
     * @param key        The key to use.
     * @param withKey    Use key as A or B.
     * @param startBlock The block to start writing, incrementing by one.
     * @param blocks     The blocks to write, must be 16 bytes each.
     * @throws IllegalArgumentException if the write operation would write beyond the sector boundary.
     */
    protected void writeWithKey(@NonNull final Key key, @NonNull final WithKey withKey, @NonNull final Block startBlock, @NonNull final byte[][] blocks) throws IOException {

        int blockIndex = Tools.blockIndexFrom(this.card, this.sector, startBlock);

        Log.i(TAG, String.format("writeWithKey - blockIndex: %s, blocks: %s", blockIndex, blocks.length));

        // ensure sector boundary (post logging)

        if (startBlock.blockNumber() + blocks.length > 4) {
            throw new IllegalArgumentException("Trying to write beyond sector boundary");
        }

        // authenticate first

        try {
            final boolean result;
            switch (withKey) {
                case A:
                    result = this.card.authenticateSectorWithKeyA(this.sector, key.getKey());
                    break;
                case B:
                    result = this.card.authenticateSectorWithKeyB(this.sector, key.getKey());
                    break;
                default:
                    result = false;
                    break;
            }

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
                Log.i(TAG, String.format("Writing block %s: %s", blockIndex, BaseEncoding.base16().encode(data)));
                this.card.writeBlock(blockIndex, data);
            } catch (final IOException e) {
                Log.w(TAG, "Failed to write", e);
                throw e;
            }

            blockIndex += 1;
        }
    }

}
