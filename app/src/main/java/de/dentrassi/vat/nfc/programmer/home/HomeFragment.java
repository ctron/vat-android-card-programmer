package de.dentrassi.vat.nfc.programmer.home;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

import de.dentrassi.vat.nfc.programmer.MainActivity;
import de.dentrassi.vat.nfc.programmer.R;
import de.dentrassi.vat.nfc.programmer.config.Configuration;
import de.dentrassi.vat.nfc.programmer.data.CreatedCard;
import de.dentrassi.vat.nfc.programmer.databinding.HomeFragmentBinding;
import de.dentrassi.vat.nfc.programmer.model.CardId;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.action.EraseAction;
import de.dentrassi.vat.nfc.programmer.nfc.action.ReadAction;
import de.dentrassi.vat.nfc.programmer.nfc.action.WriteAction;

public class HomeFragment extends Fragment {

    private static final String TAG = "MainTab";

    private HomeFragmentBinding binding;

    private enum Operation {
        /**
         * No pending operation
         */
        None,
        /**
         * Pending write operation.
         */
        Write,
        /**
         * Pending erase operation.
         */
        Erase,
    }

    private Operation scheduledOperation = Operation.None;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.home_fragment, container, false);

        this.binding = HomeFragmentBinding.bind(view);


        this.binding.memberIdInput.setSelectAllOnFocus(true);
        this.binding.cardNumberInput.setSelectAllOnFocus(true);

        this.binding.startWriteButton.setOnClickListener(this::onScheduleWrite);
        this.binding.startEraseButton.setOnClickListener(this::onScheduleErase);
        this.binding.cancelOperationButton.setOnClickListener(this::onCancelOperation);

        return view;
    }

    public void onNewIntent(final Intent intent) {
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

            case NfcAdapter.ACTION_ADAPTER_STATE_CHANGED:
                break;

            case NfcAdapter.ACTION_TAG_DISCOVERED:
                final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if (tag == null) {
                    return;
                }

                tagDiscovered(intent, tag);

                break;

            default:
                break;
        }

    }

    /**
     * Called when a tag was discovered.
     */
    private void tagDiscovered(@NonNull final Intent intent, @NonNull final Tag tag) {


        switch (this.scheduledOperation) {
            case Write: {
                try {
                    final CardId id = CardId.of(
                            Integer.parseInt(this.binding.memberIdInput.getText().toString(), 10),
                            Integer.parseInt(this.binding.cardNumberInput.getText().toString(), 10),
                            UUID.randomUUID()
                    );
                    final Keys keys = getConfiguration().getKeys().get("VAT");
                    if (keys == null) {
                        setTagText("No keys present for writing");
                        return;
                    }
                    new WriteAction(tag, keys, id, this::writeComplete).run();
                } catch (final Exception e) {
                    Log.w(TAG, "Failed to write tag", e);
                    setTagText(String.format("Failed to write tag: %s", e.getMessage()));
                }
                break;
            }
            case Erase: {
                try {
                    final Keys keys = getConfiguration().getKeys().get("VAT");
                    if (keys == null) {
                        setTagText("No keys present for writing");
                        return;
                    }
                    new EraseAction(tag, keys, this::eraseComplete).run();
                } catch (final Exception e) {
                    Log.w(TAG, "Failed to erase tag", e);
                    setTagText(String.format("Failed to erase tag: %s", e.getMessage()));
                }
                break;
            }
            case None: {
                tagDiscoveredRead(intent, tag);
                break;
            }
        }


    }

    /**
     * Set the current tag state.
     *
     * @param text the text to show
     */
    private void setTagText(final String text) {
        this.binding.tagOutput.setText(text);
    }

    /**
     * Called when a tag was discovered, and we are in read mode.
     */
    private void tagDiscoveredRead(@NonNull final Intent ignoredIntent, @NonNull final Tag tag) {
        final Keys keys = getConfiguration().getKeys().get("VAT");
        if (keys == null) {
            setTagText("Missing configuration");
            return;
        }

        new ReadAction(tag, keys, (id, ex) -> {
            if (ex != null) {
                setTagText(String.format("Failed to read tag: %s", ex.getMessage()));
                return;
            }
            tagRead(tag, id);
        }).run();
    }

    /**
     * Called when a tag was read.
     */
    private void tagRead(final Tag tag, final CardId id) {
        final StringBuilder sb = new StringBuilder("Tag discovered.").append("\n\n");

        if (id != null) {
            sb.append(String.format("Card Information: %s / %s", id.getMemberId(), id.getCardNumber()));
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
     * @param operation the new state
     */
    private void scheduleOperation(@NonNull final Operation operation) {

        if (this.scheduledOperation == operation) {
            return;
        }

        this.scheduledOperation = operation;

        if (this.scheduledOperation != Operation.None) {
            this.binding.startWriteButton.setVisibility(View.GONE);
            this.binding.startEraseButton.setVisibility(View.GONE);
            this.binding.cancelOperationButton.setVisibility(View.VISIBLE);
            this.binding.writeProgress.setVisibility(View.VISIBLE);
        } else {
            this.binding.startWriteButton.setVisibility(View.VISIBLE);
            this.binding.startEraseButton.setVisibility(View.VISIBLE);
            this.binding.cancelOperationButton.setVisibility(View.GONE);
            this.binding.writeProgress.setVisibility(View.GONE);
        }

    }

    private void writeComplete(final @Nullable CreatedCard result, @Nullable final Exception ex) {
        if (ex != null) {
            this.binding.writeOutcome.setText(String.format("Failed to write: %s", ex.getMessage()));
        } else {
            this.binding.writeOutcome.setText(R.string.message_tag_written);
        }

        scheduleOperation(Operation.None);

        if ((result != null) && (getActivity() instanceof MainActivity)) {
            ((MainActivity) getActivity()).addCard(result);
        }
    }

    private void eraseComplete(final @Nullable String result, @Nullable final Exception ex) {
        if (ex != null) {
            this.binding.writeOutcome.setText(String.format("Failed to erase: %s", ex.getMessage()));
        } else {
            this.binding.writeOutcome.setText(R.string.message_tag_ereased);
        }

        scheduleOperation(Operation.None);

        if ((result != null) && (getActivity() instanceof MainActivity)) {
            ((MainActivity) getActivity()).removeCard(result);
        }
    }

    protected @NotNull Configuration getConfiguration() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getConfiguration();
        } else {
            return new Configuration();
        }
    }

    protected void onCancelOperation(final View view) {
        writeComplete(null, new RuntimeException("Operation cancelled"));
    }

    protected void onScheduleWrite(final View view) {
        if (this.scheduledOperation == Operation.None) {
            scheduleWrite();
        }
    }

    protected void onScheduleErase(final View view) {
        if (this.scheduledOperation == Operation.None) {
            scheduleErase();
        }
    }

    protected void scheduleWrite() {
        if (getConfiguration().getKeys().get("VAT") == null) {
            this.binding.writeOutcome.setText(R.string.message_keys_not_configured);
            return;
        }

        this.binding.writeOutcome.setText("");
        scheduleOperation(Operation.Write);
    }

    protected void scheduleErase() {
        if (getConfiguration().getKeys().get("VAT") == null) {
            this.binding.writeOutcome.setText(R.string.message_keys_not_configured);
            return;
        }

        this.binding.writeOutcome.setText("");
        scheduleOperation(Operation.Erase);
    }

}