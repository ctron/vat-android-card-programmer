package de.dentrassi.vat.nfc.programmer.nfc;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.MifareClassic;
import android.os.Parcelable;
import android.util.Log;

public final class Tools {
    private static final String TAG = "NfcUtils";

    private Tools() {
    }

    public static void dumpTagData(final Parcelable[] messages) {
        for (final Parcelable p : messages) {
            if (!(p instanceof NdefMessage)) {
                Log.i(TAG, String.format("Unknown message type: %s", p.getClass()));
                continue;
            }
            final NdefMessage m = (NdefMessage) p;
            for (final NdefRecord r : m.getRecords()) {
                Log.i(TAG, String.format("Record: %s", r));
            }
        }
    }

    public static int blockIndexFrom(final MifareClassic card, final int sector, final Block block) {
        // block index
        int blockIndex = 0;
        for (int i = 0; i < sector; i++) {
            blockIndex += card.getBlockCountInSector(i);
        }

        return blockIndex + block.blockNumber();
    }

    /**
     * Split a buffer into 16 byte blocks
     *
     * @param buffer The input buffer
     * @return the output array of 16 byte blocks
     */
    public static byte[][] splitBlocks(final byte[] buffer) {
        if (buffer.length % 16 != 0) {
            throw new IllegalArgumentException(String.format("Buffer size must be 16 bytes aligned, is: %s", buffer.length));
        }

        final int blocks = buffer.length / 16;
        final byte[][] result = new byte[blocks][];
        for (int i = 0; i < blocks; i++) {
            result[i] = new byte[16];
            System.arraycopy(buffer, i * 16, result[i], 0, 16);
        }

        return result;
    }

    public static byte[] concatBlocks(byte[][] blocks) {
        int size = 0;
        for (byte[] block : blocks) {
            size += block.length;
        }

        byte[] result = new byte[size];
        int i = 0;

        for (byte[] block : blocks) {
            System.arraycopy(block, 0, result, i, block.length);
            i += block.length;
        }

        return result;
    }
}
