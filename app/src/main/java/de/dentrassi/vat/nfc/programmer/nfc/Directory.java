package de.dentrassi.vat.nfc.programmer.nfc;

import java.nio.ByteBuffer;

/**
 * Implementation of MAD1
 */
public class Directory {
    private final ByteBuffer buffer;

    private Directory() {
        this.buffer = ByteBuffer.wrap(new byte[32]);
        updateCrc();
    }

    public void setInfoSector(final int infoSector) {
        if (infoSector < 0 || infoSector > 0b1111) {
            throw new IllegalArgumentException("Info Sector for MAD1 must be between 0 and 15 (inclusive)");
        }

        this.buffer.put(1, (byte) infoSector);
    }

    public void setApplication(final int sector, final short applicationId) {
        if (sector < 1 || sector > 15) {
            throw new IllegalArgumentException("Only application ID of sector 1 till 15 (inclusive) can be set");
        }

        this.buffer.putShort(sector * 2, applicationId);
    }

    public byte[][] toData() {
        updateCrc();
        return Tools.splitBlocks(this.buffer.array());
    }

    private void updateCrc() {
        byte crc = calcCrc(this.buffer.array(), 1);
        this.buffer.put(0, crc);
    }

    private static byte calcCrc(byte[] data, final int startOffset) {
        int crc = 0xC7; // Initial CRC value

        for (int x = startOffset; x < data.length; x++) {
            byte b = data[x];

            crc ^= (b & 0xFF);

            for (int i = 0; i < 8; i++) {
                if ((crc & 0x80) != 0) {
                    crc = (crc << 1) ^ 0x1D; // Polynomial for CRC8 (x8 + x4 + x3 + x2 + 1)
                } else {
                    crc <<= 1;
                }
            }
        }

        return (byte) (crc & 0xFF);
    }

    public static Directory empty() {
        return new Directory();
    }

}
