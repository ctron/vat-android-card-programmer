package de.dentrassi.vat.nfc.programmer;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;

import java.io.IOException;

public class Scratchpad {

    public static void changeKey(Intent intent) throws Exception {
        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        final MifareClassic mifareClassic = MifareClassic.get(tag);

        try {
            mifareClassic.connect();

            // Replace these values with your actual sector and block numbers
            int sectorIndex = 1;
            int blockIndex = 0;

            // Use the default key (usually factory default) to authenticate
            byte[] defaultKey = MifareClassic.KEY_DEFAULT;
            if (mifareClassic.authenticateSectorWithKeyA(sectorIndex, defaultKey)) {
                // Authentication successful, now update the key
                byte[] newKey = {0x11, 0x22, 0x33, 0x44, 0x55, 0x66}; // Replace with your new key

                // Write the new key to the sector's key A
                mifareClassic.writeBlock(sectorIndex * 4, newKey);

                // Alternatively, you can use writeSector to update all blocks in the sector
                // mifareClassic.writeSector(sectorIndex, newKey);
            } else {
                // Authentication failed
                // Handle accordingly
            }
        } finally {
            try {
                mifareClassic.close();
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }
    }

}
