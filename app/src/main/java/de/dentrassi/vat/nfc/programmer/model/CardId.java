package de.dentrassi.vat.nfc.programmer.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class CardId {
    /**
     * Unique ID of the member (0 to 999.999 inclusive)
     */
    private final int memberId;

    /**
     * The tag's UID (max 7 bytes)
     */
    private final byte[] uid;

    private CardId(final int memberId, @NonNull final byte[] uid) throws IllegalArgumentException {
        this.memberId = memberId;
        this.uid = Objects.requireNonNull(uid);

    }

    public int getMemberId() {
        return this.memberId;
    }

    /**
     * Get the tag UID
     *
     * @return A copy of the tag UID
     */
    public byte[] getUid() {
        return this.uid.clone();
    }

    /**
     * Create a new instance
     *
     * @param memberId the member id.
     * @param uid      the tag's UID (4-7 bytes).
     * @return a new instance
     * @throws IllegalArgumentException if any of the IDs are out of range, or if the tag UID is not between 4 and 7 bytes long.
     */
    public static CardId of(final int memberId, @NonNull final byte[] uid) {
        if (memberId < 0 || memberId > 999_999) {
            throw new IllegalArgumentException("Member ID must be between 0 and 999999");
        }

        if (uid.length < 4 || uid.length > 7) {
            throw new IllegalArgumentException(String.format("UID must be between 4 and 7 bytes (both inclusive), but has a length of: %s", uid.length));
        }

        return new CardId(memberId, uid);
    }
}
