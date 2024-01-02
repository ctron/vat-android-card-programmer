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
        writeAccess();
        writeId();
    }

    private void writeAccess() throws IOException {

        // Key A: read-only
        // Key B: read-write

        final AccessBits accessBits = new AccessBits();

        accessBits.setBlockBits(Block.Block0, new BlockBits(1, 0, 0));
        accessBits.setBlockBits(Block.Block1, new BlockBits(0, 0, 0));
        accessBits.setBlockBits(Block.Block2, new BlockBits(0, 0, 0));
        accessBits.setBlockBits(Block.Block3, new BlockBits(0, 1, 1));

        final byte[] data = SectorTrailer.of(this.keys, accessBits).encode();
        final int blockIndex = Tools.blockIndexFrom(this.card, this.sector, Block.Block3);

        this.card.authenticateSectorWithKeyB(this.sector, this.keys.getB().getKey());
        this.card.writeBlock(blockIndex, data);
    }

    private void writeId() throws IOException {
        final int blockIndex = Tools.blockIndexFrom(this.card, this.sector, Block.Block0);
        byte[] data = Plain.encode(this.id);

        this.card.authenticateSectorWithKeyB(this.sector, this.keys.getB().getKey());
        this.card.writeBlock(blockIndex, data);
    }
}
