package de.dentrassi.vat.nfc.programmer.model;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.UUID;

public class CardId {
    /**
     * Unique ID of the member (0 to 999.999 inclusive)
     */
    private final int memberId;

    /**
     * Number of the card, only unique per-member (0 to 9.999 inclusive).
     */
    private final int cardNumber;

    /**
     * A random, unique card ID.
     */
    private final UUID uid;

    private CardId(final int memberId, final int cardNumber, @NonNull final UUID uid) {
        this.memberId = memberId;
        this.cardNumber = cardNumber;
        this.uid = uid;
    }

    public int getMemberId() {
        return this.memberId;
    }

    public int getCardNumber() {
        return this.cardNumber;
    }

    public UUID getUid() {
        return this.uid;
    }

    /**
     * Create a new instance
     *
     * @param memberId   the member id.
     * @param cardNumber the card number (per-member).
     * @param uid        A unique card id.
     * @return a new instance
     * @throws IllegalArgumentException if any of the IDs are out of range
     */
    public static CardId of(final int memberId, final int cardNumber, @NonNull final UUID uid) {
        if (memberId < 0 || memberId > 999_999) {
            throw new IllegalArgumentException("Member ID must be between 0 and 999999");
        }

        if (cardNumber < 0 | cardNumber > 9_999) {
            throw new IllegalArgumentException("Card number ID must be between 0 and 9999");
        }

        return new CardId(memberId, cardNumber, Objects.requireNonNull(uid));
    }

}
