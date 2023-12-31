package de.dentrassi.vat.nfc.programmer.nfc;

import androidx.annotation.NonNull;

import com.google.common.base.MoreObjects;
import com.google.common.io.BaseEncoding;

import java.util.Arrays;
import java.util.Objects;


public class AccessBits {

    /**
     * Bits for a single block
     */
    public static class BlockBits {
        public boolean c1;
        public boolean c2;
        public boolean c3;

        /**
         * Default constructor, all bits false.
         */
        public BlockBits() {
        }

        public BlockBits(boolean c1, boolean c2, boolean c3) {
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
        }

        /**
         * Convenience constructor, for using ints as the most documentations do
         */
        public BlockBits(int c1, int c2, int c3) {
            this.c1 = c1 != 0;
            this.c2 = c2 != 0;
            this.c3 = c3 != 0;
        }

        @NonNull
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("c1", c1)
                    .add("c2", c2)
                    .add("c3", c3)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockBits blockBits = (BlockBits) o;
            return c1 == blockBits.c1 && c2 == blockBits.c2 && c3 == blockBits.c3;
        }

        @Override
        public int hashCode() {
            return Objects.hash(c1, c2, c3);
        }
    }

    /**
     * Bits for the whole sector
     */
    public static class Bits {
        public final BlockBits block0;
        public final BlockBits block1;
        public final BlockBits block2;
        public final BlockBits block3;

        public Bits() {
            this.block0 = new BlockBits();
            this.block1 = new BlockBits();
            this.block2 = new BlockBits();
            // the sector trailer has a default of C1=0, C2=0, C3=1
            this.block3 = new BlockBits(false, false, true);
        }

        public Bits(
                final BlockBits block0,
                final BlockBits block1,
                final BlockBits block2,
                final BlockBits block3
        ) {
            this.block0 = block0;
            this.block1 = block1;
            this.block2 = block2;
            this.block3 = block3;
        }

        @NonNull
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("block0", block0)
                    .add("block1", block1)
                    .add("block2", block2)
                    .add("block3", block3)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bits bits = (Bits) o;
            return Objects.equals(block0, bits.block0) && Objects.equals(block1, bits.block1) && Objects.equals(block2, bits.block2) && Objects.equals(block3, bits.block3);
        }

        @Override
        public int hashCode() {
            return Objects.hash(block0, block1, block2, block3);
        }
    }

    private final byte[] bits;

    public AccessBits() {
        this.bits = new byte[4];
        // need to initialize the inverted bits
        setBits(new Bits());
        setUserData((byte) 105);
    }

    private AccessBits(@NonNull byte[] bits) {
        this.bits = bits;
    }

    public void setBits(@NonNull final Bits bits) {
        setBlockBits(Block.Block0, bits.block0);
        setBlockBits(Block.Block1, bits.block1);
        setBlockBits(Block.Block2, bits.block2);
        setBlockBits(Block.Block3, bits.block3);
    }

    public void setBlockBits(@NonNull final Block block, @NonNull final BlockBits bits) {
        switch (block) {
            case Block0:
                setBit(bits.c1, 1, 4, 0, 0);
                setBit(bits.c2, 2, 0, 0, 4);
                setBit(bits.c3, 2, 4, 1, 0);
                break;
            case Block1:
                setBit(bits.c1, 1, 5, 0, 1);
                setBit(bits.c2, 2, 1, 0, 5);
                setBit(bits.c3, 2, 5, 1, 1);
                break;
            case Block2:
                setBit(bits.c1, 1, 6, 0, 2);
                setBit(bits.c2, 2, 2, 0, 6);
                setBit(bits.c3, 2, 6, 1, 2);
                break;
            case Block3:
                setBit(bits.c1, 1, 7, 0, 3);
                setBit(bits.c2, 2, 3, 0, 7);
                setBit(bits.c3, 2, 7, 1, 3);
                break;
        }
    }

    public @NonNull BlockBits getBlockBits(@NonNull Block block) {
        switch (block) {
            case Block0: {
                boolean c1 = getBit(1, 4);
                boolean c2 = getBit(2, 0);
                boolean c3 = getBit(2, 4);
                return new BlockBits(c1, c2, c3);
            }
            case Block1: {
                boolean c1 = getBit(1, 5);
                boolean c2 = getBit(2, 1);
                boolean c3 = getBit(2, 5);
                return new BlockBits(c1, c2, c3);
            }
            case Block2: {
                boolean c1 = getBit(1, 6);
                boolean c2 = getBit(2, 2);
                boolean c3 = getBit(2, 6);
                return new BlockBits(c1, c2, c3);
            }
            case Block3: {
                boolean c1 = getBit(1, 7);
                boolean c2 = getBit(2, 3);
                boolean c3 = getBit(2, 7);
                return new BlockBits(c1, c2, c3);
            }
            default:
                throw new IllegalArgumentException("Unknown block");
        }
    }

    public @NonNull Bits getBits() {
        return new Bits(
                getBlockBits(Block.Block0),
                getBlockBits(Block.Block1),
                getBlockBits(Block.Block2),
                getBlockBits(Block.Block3)
        );
    }

    private void setBit(boolean value, int bytePos, int bitPos, int negBytePos, int negBitPos) {
        setSingleBit(value, bytePos, bitPos);
        setSingleBit(!value, negBytePos, negBitPos);
    }

    private void setSingleBit(boolean value, int pos, int bit) {
        if (value) {
            this.bits[pos] |= (1 << bit);
        } else {
            this.bits[pos] &= ~(1 << bit);
        }
    }

    private boolean getBit(int pos, int bit) {
        return (this.bits[pos] & (1 << bit)) > 0;
    }

    public @NonNull byte[] getRawBits() {
        return this.bits.clone();
    }

    public byte getUserData() {
        return this.bits[3];
    }

    public void setUserData(byte userData) {
        this.bits[3] = userData;
    }

    public static @NonNull AccessBits fromData(@NonNull byte[] bits) {
        if (bits.length != 4) {
            throw new IllegalArgumentException("Access bits must have a length of 4 bits");
        }

        return new AccessBits(bits);
    }


    public static @NonNull AccessBits fromString(@NonNull String data) {
        return fromData(BaseEncoding.base16().decode(data));
    }

    @NonNull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bits", getBits())
                .add("userData", getUserData())
                .toString();
    }

    @NonNull
    public AccessBits copy() {
        return new AccessBits(this.bits.clone());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessBits that = (AccessBits) o;
        return Arrays.equals(bits, that.bits);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bits);
    }
}
