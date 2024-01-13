package de.dentrassi.vat.nfc.programmer.model;

import androidx.annotation.NonNull;

/**
 * The information to write on a card.
 */
final public class WriteCardInformation {
    private final int memberId;
    private final AdditionalInformation additional;

    private WriteCardInformation(int memberId, @NonNull AdditionalInformation additional) {
        this.memberId = memberId;
        this.additional = additional;
    }

    public int getMemberId() {
        return this.memberId;
    }

    public AdditionalInformation getAdditional() {
        return this.additional;
    }

    public static WriteCardInformation of(int memberId, @NonNull AdditionalInformation additional) {
        return new WriteCardInformation(memberId, additional);
    }
}
