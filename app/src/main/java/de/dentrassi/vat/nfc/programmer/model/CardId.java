package de.dentrassi.vat.nfc.programmer.model;

import androidx.annotation.NonNull;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class CardId {
    /**
     * Unique ID of the member (0 to 999.999 inclusive)
     */
    private final int memberId;

    /**
     * The tag's UID (max 7 bytes)
     */
    private final Uid uid;

    private CardId(final int memberId, @NonNull final Uid uid) throws IllegalArgumentException {
        this.memberId = memberId;
        this.uid = uid;
    }

    public int getMemberId() {
        return this.memberId;
    }

    /**
     * Get the tag UID
     *
     * @return A copy of the tag UID
     */
    public Uid getUid() {
        return this.uid;
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
        return of(memberId, Uid.of(uid));
    }

    /**
     * Create a new instance
     *
     * @param memberId the member id.
     * @param uid      the tag's UID (4-7 bytes).
     * @return a new instance
     * @throws IllegalArgumentException if any of the IDs are out of range, or if the tag UID is not between 4 and 7 bytes long.
     */
    public static CardId of(final int memberId, @NonNull final Uid uid) {
        if (memberId < 0 || memberId > 999_999) {
            throw new IllegalArgumentException(String.format("Member ID must be between 0 and 999999 (was: %s)", memberId));
        }

        return new CardId(memberId, uid);
    }

    /**
     * Check if the card is empty, all zeroes (for UID and member ID).
     *
     * @return {@code true} if the card is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        if (!this.uid.isEmpty()) {
            return false;
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
        var uid = this.uid.getUid();
        int len = Math.max(tagId.length, uid.length);

        for (int i = 0; i < len; i++) {
            final byte a = i >= uid.length ? 0 : uid[i];
            final byte b = i >= tagId.length ? 0 : tagId[i];

            if (a != b) {
                return false;
            }
        }

        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("memberId", this.memberId)
                .add("uid", this.uid)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CardId cardId = (CardId) o;
        return this.memberId == cardId.memberId && Objects.equals(this.uid, cardId.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.memberId, this.uid);
    }
}
