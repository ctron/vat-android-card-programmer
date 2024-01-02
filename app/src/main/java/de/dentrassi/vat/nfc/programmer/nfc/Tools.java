package de.dentrassi.vat.nfc.programmer.nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Parcelable;
import android.util.Log;

import com.google.common.io.BaseEncoding;

public final class Tools {
    private static final String TAG = "NfcUtils";

    private Tools() {
    }

    public static String getUid(final Intent intent) {
        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return null;
        }
        return BaseEncoding.base16().encode(tag.getId());
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

    public static int blockIndexFrom(final MifareClassic card, final int sector, final int blockNumber) {
        // block index
        int blockIndex = 0;
        for (int i = 0; i < sector; i++) {
            blockIndex += card.getBlockCountInSector(i);
        }

        return blockIndex;
    }
}
