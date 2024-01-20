package de.dentrassi.vat.nfc.programmer.nfc;

/**
 * Application IDs used in the {@link Directory}.
 * <p>
 * <strong>NOTE: </strong> These are not officially registered application IDs!
 */
public final class ApplicationIds {


    public static final short CARD_HOLDER = of((byte) 0x04, (byte) 0x00);
    public static final short VAT = of((byte) 1, (byte) 0x78);

    private ApplicationIds() {
    }

    // FIXME: swap function and application
    public static short of(byte function, byte application) {
        return (short) (((function & 0xFF) << 8 | (application & 0xFF)) & 0xFFFF);
    }

    // FIXME: swap function and application
    public static short of(int function, int application) {
        return (short) (((function & 0xFF) << 8 | (application & 0xFF)) & 0xFFFF);
    }
}
