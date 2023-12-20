package de.dentrassi.vat.nfc.programmer;

import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

public class NdefReader extends TagAction<NdefMessage> {

    private static final String TAG = "Reader";

    private final Tag tag;

    public NdefReader(final Tag tag, final BiConsumer<NdefMessage, Exception> outcome) {
        super(tag, outcome);
        this.tag = tag;
    }

    protected NdefMessage process() throws Exception {
        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        m.connect();
        try {

            int block = 4;
            int sector = -1;

            final ByteBuffer buf = ByteBuffer.allocate(m.getSize());

            while (block < m.getBlockCount()) {
                int nextSector = m.blockToSector(block);
                if (nextSector != sector) {
                    sector = nextSector;
                    m.authenticateSectorWithKeyA(sector, MifareClassic.KEY_NFC_FORUM);
                }

                buf.put(m.readBlock(block));

                for (int i = 0; i < 16; i++) {
                    try {
                        int len = buf.position() - i;
                        final byte[] current = new byte[len];
                        System.arraycopy(buf.array(), 0, current, 0, len);
                        return new NdefMessage(current);
                    } catch (final Exception e) {
                        // continue reading
                        Log.i(TAG, String.format("After block %s (-%s): %s", block, i, e.getMessage()));
                    }
                }

                block++;
            }

            Log.i(TAG, String.format("Read: %s bytes", buf.position()));

            throw new RuntimeException("Failed to decode message");

        } finally {
            m.close();
        }
    }
}
