package de.dentrassi.vat.nfc.programmer.codec;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import de.dentrassi.vat.nfc.programmer.data.CardId;

public final class Plain {
    private Plain() {
    }

    @NonNull
    public static byte[] encode(@NonNull CardId id) {
        final String data = String.format(Locale.ENGLISH, "%06d%04d", id.getMemberId(), id.getCardNumber());

        final ByteBuffer out = ByteBuffer.wrap(new byte[16]);
        StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(data), out, true);

        return out.array();
    }

    @NonNull
    public static CardId decode(@NonNull byte[] data) throws Exception {
        final CharBuffer buffer = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(data));

        final char[] member = new char[6];
        final char[] card = new char[4];
        buffer.get(member);
        buffer.get(card);

        ensureDigits(member);
        ensureDigits(card);

        return CardId.of(
                Integer.parseInt(String.valueOf(member), 10),
                Integer.parseInt(String.valueOf(card), 10)
        );
    }

    private static void ensureDigits(char[] data) {
        for (final char c : data) {
            if (!Character.isDigit(c)) {
                throw new IllegalArgumentException("Must only be digits");
            }
        }
    }
}
