package de.dentrassi.vat.nfc.programmer.nfc;

public enum Block {
    Block0,
    Block1,
    Block2,
    Block3;

    /**
     * Get the number of the bock (0 to 3).
     *
     * @return The block number
     */
    public int blockNumber() {
        return this.ordinal();
    }
}
