package de.dentrassi.vat.nfc.programmer.codec;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

import de.dentrassi.vat.nfc.programmer.model.CardId;

/**
 * The standard (plain) encoder.
 */
public final class Plain {

    private Plain() {
    }

    @NonNull
    public static byte[] encode(@NonNull final CardId id) throws IllegalStateException {

        // block 0

        final ByteBuffer out = ByteBuffer.wrap(new byte[16]);

        out.putInt(id.getMemberId());
        out.put(id.getUid());
        // NOTE: if we encode more data, we need to pad this UID to 7 bytes, right now it doesn't matter

        // return result

        return out.array();
    }

    @NonNull
    public static CardId decode(@NonNull final byte[] data) throws Exception {

        // block 0

        final ByteBuffer buffer = ByteBuffer.wrap(data);
        final int memberId = buffer.getInt();
        final byte[] uid = new byte[7];
        buffer.get(uid);

        // return result

        return CardId.of(
                memberId,
                uid
        );
    }
}
