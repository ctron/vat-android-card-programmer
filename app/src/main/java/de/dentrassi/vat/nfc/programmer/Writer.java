package de.dentrassi.vat.nfc.programmer;

import android.app.Activity;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.BiConsumer;

public class Writer extends TagAction<Void> {

    private static final String TAG = "Writer";

    private final Data data;

    public Writer(@NonNull final Activity activity, @NonNull final Tag tag, @NonNull final Data data, @NonNull final BiConsumer<Void, Exception> outcome) {
        super(tag, activity, outcome);
        this.data = data;
    }

    protected Void process() throws Exception {
        Log.i(TAG, "Start writing tag");

        // final NdefFormatable n = getTagAs(NdefFormatable::get, "NDEF support");
        final MifareClassic m = getTagAs(MifareClassic::get, "Mifare Classic");

        /*
        n.connect();
        try {
            n.format(data.toNdefMessage());
        } finally {
            n.close();
        }*/

        m.connect();
        try {
            final ByteBuffer raw = ByteBuffer.wrap(data.toNdefMessage().toByteArray());

            Log.i(TAG, String.format("Writing %s bytes", raw.remaining()));

            byte[] buf = new byte[16];

            int block = 4;
            int sector = -1;

            while (raw.hasRemaining()) {
                int nextSector = m.blockToSector(block);
                if (nextSector != sector) {
                    sector = nextSector;
                    m.authenticateSectorWithKeyA(sector, MifareClassic.KEY_NFC_FORUM);
                }

                int len = 16;
                if (raw.remaining() < len) {
                    Arrays.fill(buf, (byte) 0);
                    len = raw.remaining();
                }
                raw.get(buf, 0, len);

                Log.i(TAG, String.format("Writing block %s, sector: %s", block, sector));

                m.writeBlock(block, buf);
                block++;
            }


        } finally {
            m.close();
        }

        return null;
    }


}
