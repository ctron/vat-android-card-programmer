package de.dentrassi.vat.nfc.programmer.data;

public class CardId {
    /**
     * Unique ID of the member (0 to 999.999 inclusive)
     */
    private final int memberId;

    /**
     * Number of the card, only unique per-member (0 to 9.999 inclusive).
     */
    private final int cardNumber;

    private CardId(int memberId, int cardNumber) {
        this.memberId = memberId;
        this.cardNumber = cardNumber;
    }

    public int getMemberId() {
        return memberId;
    }

    public int getCardNumber() {
        return cardNumber;
    }

    /**
     * Create a new instance
     *
     * @param memberId   the member id.
     * @param cardNumber the card number (per-member)
     * @return a new instance
     * @throws IllegalArgumentException if any of the IDs are out of range
     */
    public static CardId of(int memberId, int cardNumber) {
        if (memberId < 0 || memberId > 999_999) {
            throw new IllegalArgumentException("Member ID must be between 0 and 999999");
        }

        if (cardNumber < 0 | cardNumber > 9_999) {
            throw new IllegalArgumentException("Card number ID must be between 0 and 9999");
        }

        return new CardId(memberId, cardNumber);
    }


}
