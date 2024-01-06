package de.dentrassi.vat.nfc.programmer.codec;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import de.dentrassi.vat.nfc.programmer.model.CardId;

/**
 * The standard (plain) encoder.
 */
public final class Plain {

    private Plain() {
    }

    @NonNull
    public static byte[][] encode(@NonNull final CardId id) throws IllegalStateException {

        // block 0

        final ByteBuffer out0 = ByteBuffer.wrap(new byte[16]);

        final String data = String.format(Locale.ENGLISH, "%06d%04d", id.getMemberId(), id.getCardNumber());
        StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(data), out0, true);

        if (out0.position() != 10) {
            throw new IllegalStateException(String.format(Locale.ENGLISH, "Card ID information should be exactly 10 bytes, but is %s", out0.position()));
        }

        // put 6 null bytes, Java initializes arrays with zero
        out0.put(new byte[6]);

        // block 1

        final ByteBuffer out1 = ByteBuffer.wrap(new byte[16]);

        out1.putLong(id.getUid().getMostSignificantBits());
        out1.putLong(id.getUid().getLeastSignificantBits());

        // return result

        return new byte[][]{out0.array(), out1.array()};
    }

    @NonNull
    public static CardId decode(@NonNull final byte[] block0, @NonNull final byte[] block1) throws Exception {

        // block 0

        final CharBuffer buffer0 = StandardCharsets.US_ASCII.newDecoder().decode(ByteBuffer.wrap(block0));

        final char[] member = new char[6];
        final char[] card = new char[4];
        buffer0.get(member);
        buffer0.get(card);

        ensureDigits(member);
        ensureDigits(card);

        // block 1

        final ByteBuffer buffer1 = ByteBuffer.wrap(block1);
        final UUID uid = new UUID(buffer1.getLong(), buffer1.getLong());

        // return result

        return CardId.of(
                Integer.parseInt(String.valueOf(member), 10),
                Integer.parseInt(String.valueOf(card), 10),
                uid
        );
    }

    private static void ensureDigits(final char[] data) {
        for (final char c : data) {
            if (!Character.isDigit(c)) {
                throw new IllegalArgumentException("Must only be digits");
            }
        }
    }
}
