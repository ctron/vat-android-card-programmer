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

    /**
     * Check if an ID is empty, all zeroes.
     *
     * @return {@code true} if the card is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        for (byte b : this.uid) {
            if (b > 0) {
                return false;
            }
        }

        return this.memberId == 0;
    }

    /**
     * Ensure that the encoded UID matches the tag UID.
     *
     * @param tagId The UID from the tag
     * @return {@code true} if the UIDs match, {@code false} otherwise.
     */
    public boolean validateUid(byte[] tagId) {
        int len = Math.max(tagId.length, this.uid.length);

        for (int i = 0; i < len; i++) {
            byte a = i >= this.uid.length ? 0 : this.uid[i];
            byte b = i >= tagId.length ? 0 : tagId[i];

            if (a != b) {
                return false;
            }
        }

        return true;
    }
}
