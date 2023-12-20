package de.dentrassi.vat.nfc.programmer.list;

import de.dentrassi.vat.nfc.programmer.data.CardId;

public class CreatedCard {
    private final String uid;
    private final CardId id;

    public CreatedCard(String uid, CardId id) {
        this.uid = uid;
        this.id = id;
    }

    public CardId getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }
}
