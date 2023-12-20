package de.dentrassi.vat.nfc.programmer.nfc;

import static de.dentrassi.vat.nfc.programmer.nfc.AccessBits.BlockBits;

import android.nfc.tech.MifareClassic;

import java.io.IOException;

import de.dentrassi.vat.nfc.programmer.codec.Plain;
import de.dentrassi.vat.nfc.programmer.data.CardId;

/**
 * Write information to card
 */
public class Writer {
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
            this.performInternal();
        } finally {
            this.card.close();
        }
    }

    private void performInternal() throws Exception {
        // writeAccess();
        writeId();
    }

    private void writeAccess(int sector) {

        // A = not used
        // B = private key

        final BlockBits dataBlockBits = new BlockBits(false, true, true);
        final BlockBits trailerBlockBits = new BlockBits(false, true, true);

        final AccessBits accessBits = new AccessBits();
        accessBits.setBlockBits(AccessBits.Block.First, dataBlockBits);
        accessBits.setBlockBits(AccessBits.Block.Fourth, trailerBlockBits);

    }

    private void writeId() throws IOException {

        final int blockIndex = Tools.blockIndexFrom(this.card, this.sector, 0);
        byte[] data = Plain.encode(this.id);

        this.card.authenticateSectorWithKeyA(this.sector, this.keys.getA().getKey());
        this.card.writeBlock(blockIndex, data);
    }
}
