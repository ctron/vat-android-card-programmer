package de.dentrassi.vat.nfc.programmer.nfc.action;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class Data {

    private static final String TAG = "Data";

    public static final String DOMAIN = "de.dentrassi.vat";
    public static final String TYPE = "code";
    private static final String URI = "vnd.android.nfc://ext/" + DOMAIN + ":" + TYPE;

    private final String code;

    private Data(final String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public static Data newData(final String code) {
        return new Data(code);
    }

    /**
     * Convert the data into an {@link NdefMessage}.
     *
     * @return The data encoded as {@link NdefMessage}.
     */
    @NonNull
    public NdefMessage toNdefMessage() {
        final NdefRecord record = NdefRecord.createExternal(DOMAIN, TYPE, code.getBytes(StandardCharsets.UTF_8));
        return new NdefMessage(record);
    }

    public static Optional<Data> fromNdefMessage(Parcelable[] messages) {

        for (final Parcelable p : messages) {
            if (!(p instanceof NdefMessage)) {
                Log.i(TAG, String.format("Unknown message type: %s", p.getClass()));
                continue;
            }

            final NdefMessage m = (NdefMessage) p;
            for (final NdefRecord r : m.getRecords()) {
                Log.i(TAG, String.format("Record: %s", r));
                if (r.getTnf() == NdefRecord.TNF_EXTERNAL_TYPE) {

                    final String uri = r.toUri().toString();
                    Log.i(TAG, String.format("Uri: %s", r));

                    if (!uri.equals(URI)) {
                        continue;
                    }

                    try {
                        final String payload = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(r.getPayload())).toString();
                        return Optional.of(newData(payload));
                    } catch (final Exception e) {
                        Log.w(TAG, "Failed to decode matching record", e);
                    }
                }
            }
        }

        return Optional.empty();
    }
}
