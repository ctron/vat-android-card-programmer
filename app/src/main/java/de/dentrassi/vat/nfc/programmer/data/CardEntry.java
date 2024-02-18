package de.dentrassi.vat.nfc.programmer.data;

import androidx.annotation.NonNull;

import com.google.common.base.MoreObjects;

import java.time.ZonedDateTime;
import java.util.Objects;

import de.dentrassi.vat.nfc.programmer.model.AdditionalInformation;
import de.dentrassi.vat.nfc.programmer.model.CardId;
import de.dentrassi.vat.nfc.programmer.model.Uid;

public final class CardEntry {
    private final @NonNull CardId id;
    private final @NonNull AdditionalInformation additional;
    private final @NonNull ZonedDateTime timestamp;
    private final boolean erased;

    private CardEntry(
            @NonNull final CardId id,
            @NonNull final AdditionalInformation additional,
            @NonNull final ZonedDateTime timestamp,
            boolean erased
    ) {
        this.id = Objects.requireNonNull(id);
        this.additional = Objects.requireNonNull(additional);
        this.timestamp = Objects.requireNonNull(timestamp);
        this.erased = erased;
    }

    public @NonNull CardId getId() {
        return this.id;
    }

    @NonNull
    public AdditionalInformation getAdditional() {
        return this.additional;
    }

    @NonNull
    public ZonedDateTime getTimestamp() {
        return this.timestamp;
    }

    public boolean isErased() {
        return this.erased;
    }

    /**
     * Create a new card entry for a provisioned card.
     *
     * @param id         The card ID
     * @param additional Additional card information
     * @param timestamp  The timestamp of the operation
     * @return the new instance
     */
    public static CardEntry ofCreated(
            @NonNull final CardId id,
            @NonNull final AdditionalInformation additional,
            @NonNull final ZonedDateTime timestamp) {
        return new CardEntry(id, additional, timestamp, false);
    }

    /**
     * Create a new entry for a erased card
     *
     * @param uid       The UID of the tag
     * @param timestamp The timestamp of the operation
     * @return the new instance
     */
    public static CardEntry ofErased(
            @NonNull final Uid uid,
            @NonNull final ZonedDateTime timestamp) {
        return new CardEntry(CardId.of(0, uid), AdditionalInformation.ofDefault(), timestamp, true);
    }

    /**
     * Create a new card entry for an entry loaded from the storage
     *
     * @param id         The card ID
     * @param erased     Flag if the entry is erased
     * @param additional Additional card information
     * @param timestamp  The timestamp of the operation
     * @return the new instance
     */
    public static CardEntry ofStored(
            @NonNull final CardId id,
            boolean erased,
            @NonNull final AdditionalInformation additional,
            @NonNull final ZonedDateTime timestamp) {
        if (erased) {
            return ofErased(id.getUid(), timestamp);
        } else {
            return ofCreated(id, additional, timestamp);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .add("additional", this.additional)
                .add("timestamp", this.timestamp)
                .add("erased", this.erased)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardEntry cardEntry = (CardEntry) o;
        return this.erased == cardEntry.erased && Objects.equals(this.id, cardEntry.id) && Objects.equals(this.additional, cardEntry.additional) && Objects.equals(this.timestamp, cardEntry.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.additional, this.timestamp, this.erased);
    }
}
