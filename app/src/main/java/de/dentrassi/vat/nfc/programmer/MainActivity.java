package de.dentrassi.vat.nfc.programmer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import de.dentrassi.vat.nfc.programmer.data.CardId;
import de.dentrassi.vat.nfc.programmer.nfc.Tools;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private NfcAdapter adapter;
    private TextView textView;
    private Button writeButton;
    private ProgressBar writeProgress;
    private boolean writeScheduled;
    private TextView writeOutcome;

    private EditText memberIdInput;
    private EditText cardNumberInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.textView = findViewById(R.id.tagOutput);
        this.writeButton = findViewById(R.id.startWriteButton);
        this.writeProgress = findViewById(R.id.writeProgress);
        this.writeOutcome = findViewById(R.id.writeOutcome);

        this.memberIdInput = findViewById(R.id.memberIdInput);
        this.cardNumberInput = findViewById(R.id.cardNumberInput);

        this.memberIdInput.setSelectAllOnFocus(true);
        this.cardNumberInput.setSelectAllOnFocus(true);

        initNfc();
    }

    private void initNfc() {
        final NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        this.adapter = manager.getDefaultAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setWriteScheduled(false, true);
    }

    private void enableForegroundDispatch() {
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        final ArrayList<IntentFilter> f = new ArrayList<>();
        if (!this.writeScheduled) {
            f.add(new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED));
        } else {
            // we actually use the same intent
            f.add(new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED));
        }

        this.adapter.enableForegroundDispatch(this, pendingIntent, f.toArray(new IntentFilter[0]), null);
    }

    @Override
    protected void onPause() {
        disableForegroundDispatch();
        super.onPause();
    }

    private void disableForegroundDispatch() {
        this.adapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        Log.i(TAG, String.format("New Intent: %s", intent));
        Log.i(TAG, String.format("   Action: %s", intent.getAction()));
        Log.i(TAG, String.format("   Data: %s", intent.getData()));
        Log.i(TAG, String.format("   DataString: %s", intent.getDataString()));
        Log.i(TAG, "   Extras:");

        final Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.keySet() != null) {
            for (final String key : bundle.keySet()) {
                final Object value = bundle.get(key);
                Log.i(TAG, String.format("       %s: %s", key, value));
            }
        }

        if (intent.getAction() == null) {
            return;
        }

        switch (intent.getAction()) {

            case NfcAdapter.ACTION_TAG_DISCOVERED:
                final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if (tag == null) {
                    return;
                }

                if (this.writeScheduled) {
                    try {
                        CardId id = CardId.of(
                                Integer.parseInt(this.memberIdInput.getText().toString(), 10),
                                Integer.parseInt(this.cardNumberInput.getText().toString(), 10)
                        );
                        new Writer(this, tag, id, this::writeComplete).run();
                    } catch (final Exception e) {
                        Log.w(TAG, "Failed to write tag", e);
                        setTagText(String.format("Failed to write tag: %s", e.getMessage()));
                    }
                } else {
                    tagScanned(intent, tag);
                }

                break;

            default:
                break;
        }

    }

    /**
     * Set the current tag state.
     *
     * @param text the text to show
     */
    private void setTagText(final String text) {
        this.textView.setText(text);
    }

    private void tagScanned(@NonNull final Intent intent, @NonNull final Tag tag) {
        new Reader(tag, this, (m, ex) -> {
            if (ex != null) {
                setTagText(String.format("Failed to read tag: %s", ex.getMessage()));
            }
            tagRead(tag, m);
        }).run();
    }

    /// Handle a scanned NDEF tag
    private void tagNdefScanned(@NonNull final Intent intent, @NonNull final Tag tag) {

        final Parcelable[] par = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (par != null) {

            final NdefMessage[] messages = Arrays.stream(par).flatMap(p -> {
                if (p instanceof NdefMessage) {
                    return Stream.of((NdefMessage) p);
                } else {
                    return Stream.empty();
                }
            }).toArray(NdefMessage[]::new);
            tagNdefRead(tag, messages);

        } else {

            new NdefReader(tag, this, (m, ex) -> {
                if (ex != null) {
                    setTagText(String.format("Failed to read tag: %s", ex.getMessage()));
                }
                if (m != null) {
                    tagNdefRead(tag, new NdefMessage[]{m});
                }
            }).run();

        }
    }

    private void tagNdefRead(final Tag tag, final NdefMessage[] messages) {

        final StringBuilder sb = new StringBuilder("Tag discovered.");

        Tools.dumpTagData(messages);

        final Optional<Data> data = Data.fromNdefMessage(messages);
        sb.append("\n\n");
        if (!data.isPresent()) {
            sb.append("No VAT data detected");
        }
        data.ifPresent(d -> {
            sb.append(String.format("Tag data: %s", d.getCode()));
        });

        if (tag.getTechList() != null) {
            if (Arrays.stream(tag.getTechList()).anyMatch(tech -> MifareClassic.class.getName().equals(tech))) {
                sb.append("\n\nTag can be used for access control.");
            }
        }

        setTagText(sb.toString());
    }

    private void tagRead(final Tag tag, Optional<CardId> id) {
        final StringBuilder sb = new StringBuilder("Tag discovered.").append("\n\n");

        if (id.isPresent()) {
            sb.append(String.format("Card Information: %s / %s", id.get().getMemberId(), id.get().getCardNumber()));
        } else {
            sb.append("No ID data detected");
        }

        if (tag.getTechList() != null) {
            if (Arrays.stream(tag.getTechList()).anyMatch(tech -> MifareClassic.class.getName().equals(tech))) {
                sb.append("\n\nTag can be used for access control.");
            }
        }

        setTagText(sb.toString());
    }

    /**
     * Set the write-scheduled state
     *
     * @param state the new state
     */
    private void setWriteScheduled(boolean state) {
        setWriteScheduled(state, false);
    }

    /**
     * Set the write-scheduled state
     *
     * @param state the new state
     * @param force force the update
     */
    private void setWriteScheduled(boolean state, boolean force) {
        if (!force && this.writeScheduled == state) {
            return;
        }

        disableForegroundDispatch();

        this.writeScheduled = state;
        if (this.writeScheduled) {
            this.writeButton.setText(R.string.button_cancel_write);
            this.writeProgress.setVisibility(View.VISIBLE);
        } else {
            this.writeButton.setText(R.string.button_start_write);
            this.writeProgress.setVisibility(View.GONE);
        }

        enableForegroundDispatch();
    }

    private void writeComplete(final Void result, final Exception ex) {
        if (ex != null) {
            this.writeOutcome.setText(String.format("Failed to write: %s", ex.getMessage()));
        } else {
            this.writeOutcome.setText("Tag written");
        }

        setWriteScheduled(false);
    }

    public void onScheduleWrite(final View view) {
        if (!this.writeScheduled) {
            this.writeOutcome.setText("");
            setWriteScheduled(true);
        } else {
            writeComplete(null, new RuntimeException("Write cancelled"));
        }
    }
}