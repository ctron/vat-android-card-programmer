package de.dentrassi.vat.nfc.programmer.home;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.common.io.BaseEncoding;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import de.dentrassi.vat.nfc.programmer.MainActivity;
import de.dentrassi.vat.nfc.programmer.R;
import de.dentrassi.vat.nfc.programmer.config.Configuration;
import de.dentrassi.vat.nfc.programmer.data.CreatedCard;
import de.dentrassi.vat.nfc.programmer.databinding.HomeFragmentBinding;
import de.dentrassi.vat.nfc.programmer.model.AdditionalInformation;
import de.dentrassi.vat.nfc.programmer.model.CardId;
import de.dentrassi.vat.nfc.programmer.model.IdType;
import de.dentrassi.vat.nfc.programmer.model.WriteCardInformation;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.action.EraseAction;
import de.dentrassi.vat.nfc.programmer.nfc.action.ReadAction;
import de.dentrassi.vat.nfc.programmer.nfc.action.WriteAction;
import de.dentrassi.vat.nfc.programmer.utils.TextWatcherAdapter;
import de.dentrassi.vat.nfc.programmer.utils.validation.Error;
import de.dentrassi.vat.nfc.programmer.utils.validation.Ok;
import de.dentrassi.vat.nfc.programmer.utils.validation.Result;
import de.dentrassi.vat.nfc.programmer.utils.validation.TextValidator;

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
    public View onCreateView(final @NonNull LayoutInflater inflater, final @Nullable ViewGroup container, final @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.home_fragment, container, false);

        this.binding = HomeFragmentBinding.bind(view);

        this.binding.memberIdInput.setSelectAllOnFocus(true);
        this.binding.memberIdInput.addTextChangedListener(new TextValidator(this.binding.layoutMemberIdInput) {
            @Override
            protected @NonNull Result validate(final String value) {

                validateInput();

                if (value.isEmpty()) {
                    return Error.of(getString(R.string.validation_error_must_not_be_empty));
                }

                return Ok.of();
            }
        });

        this.binding.holderIdType.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(final Editable e) {
                validateInput();
            }
        });

        this.binding.startWriteButton.setOnClickListener(this::onScheduleWrite);
        this.binding.startEraseButton.setOnClickListener(this::onScheduleErase);
        this.binding.cancelOperationButton.setOnClickListener(this::onCancelOperation);
        if (getContext() != null) {
            this.binding.holderIdType.setText(getContext().getResources().getStringArray(R.array.id_types)[0], false);
        }

        validateInput();
        configChanged();

        return view;
    }

    private void validateInput() {
        this.binding.startWriteButton.setEnabled(canWrite());

        final IdType type = IdType.fromLocalizedText(getContext(), binding.holderIdType.getText());
        this.binding.holderIdTypeWrapper.setEnabled(type != IdType.None);

        // FIXME: find something to validate
    }

    private boolean canWrite() {
        if (this.binding.memberIdInput.getText() == null || this.binding.memberIdInput.getText().length() == 0) {
            this.binding.startWriteButton.setEnabled(false);
            return false;
        }

        return true;
    }

    public void configChanged() {
        if (getConfiguration().getOrganizations().isEmpty()) {
            this.binding.warningMissingConfiguration.setVisibility(View.VISIBLE);
            this.binding.startWriteButton.setEnabled(false);
            this.binding.startEraseButton.setEnabled(false);
        } else {
            this.binding.warningMissingConfiguration.setVisibility(View.GONE);
            this.binding.startWriteButton.setEnabled(canWrite());
            this.binding.startEraseButton.setEnabled(true);
        }
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
                    Log.w(TAG, "Tag discovered, but provides no instance");
                    return;
                }

                try {
                    tagDiscovered(intent, tag);
                } catch (final Exception e) {
                    Log.w(TAG, "Failed to initiate operation", e);
                    writeComplete(null, e);
                }

                break;

            default:
                break;
        }

    }

    private static String getOrDefault(final @Nullable Editable editable) {
        if (editable != null) {
            return editable.toString();
        } else {
            return "";
        }
    }

    /**
     * Called when a tag was discovered.
     */
    private void tagDiscovered(@NonNull final Intent intent, @NonNull final Tag tag) {
        Log.d(TAG, String.format("Tag discovered - operation: %s", this.scheduledOperation));

        switch (this.scheduledOperation) {
            case Write: {
                try {
                    final WriteCardInformation information = WriteCardInformation.of(
                            Integer.parseInt(getOrDefault(this.binding.memberIdInput.getText()), 10),
                            AdditionalInformation.of(
                                    getOrDefault(this.binding.holderName.getText()),
                                    getOrDefault(this.binding.holderId.getText()),
                                    IdType.fromLocalizedText(getContext(), this.binding.holderIdType.getText())
                            )
                    );
                    final Keys keys = getConfiguration().getKeysFor("VAT");
                    if (keys == null) {
                        setTagText(getString(R.string.error_no_keys_present_for_writing));
                        return;
                    }
                    new WriteAction(tag, keys, information, this::writeComplete).run();
                } catch (final Exception e) {
                    Log.w(TAG, "Failed to write tag", e);
                    setTagText(String.format("Failed to write tag: %s", e.getMessage()));
                    writeComplete(null, e);
                }
                break;
            }
            case Erase: {
                try {
                    final Keys keys = getConfiguration().getKeysFor("VAT");
                    if (keys == null) {
                        setTagText(getString(R.string.error_no_keys_present_for_writing));
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
        final Keys keys = getConfiguration().getKeysFor("VAT");
        if (keys == null) {
            setTagText(getString(R.string.error_missing_configuration));
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
        final StringBuilder sb = new StringBuilder(getString(R.string.message_tag_discovered)).append("\n\n");

        if (id != null) {
            sb.append(String.format("Card Information: %s / %s", id.getMemberId(), BaseEncoding.base16().encode(id.getUid())));
        } else {
            sb.append(getString(R.string.message_no_id_data_detected));
        }

        if (tag.getTechList() != null) {
            if (Arrays.stream(tag.getTechList()).anyMatch(tech -> MifareClassic.class.getName().equals(tech))) {
                sb.append("\n\n").append(getString(R.string.message_tag_can_be_used_for_access_control));
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

        Log.d(TAG, "schedule operation: " + operation);

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
        Log.d(TAG, String.format("writeComplete - result: %s, ex: %s", result, ex));

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

    private void eraseComplete(@Nullable byte[] result, @Nullable final Exception ex) {
        Log.d(TAG, String.format("erase - result: %s, ex: %s", result == null ? "<null>" : BaseEncoding.base16().encode(result), ex));

        if (ex != null) {
            this.binding.writeOutcome.setText(String.format(getString(R.string.message_failed_to_erase), ex.getMessage()));
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
        Log.d(TAG, "request to cancel operation");
        writeComplete(null, new RuntimeException(getString(R.string.message_operation_cancelled)));
    }

    protected void onScheduleWrite(final View view) {
        Log.d(TAG, "requested write operation");
        if (this.scheduledOperation == Operation.None) {
            scheduleWrite();
        }
    }

    protected void onScheduleErase(final View view) {
        Log.d(TAG, "requested erase operation");

        if (this.scheduledOperation == Operation.None) {
            scheduleErase();
        }
    }

    protected void scheduleWrite() {
        Log.d(TAG, "schedule write operation");

        if (getConfiguration().getKeysFor("VAT") == null) {
            this.binding.writeOutcome.setText(R.string.message_keys_not_configured);
            return;
        }

        this.binding.writeOutcome.setText("");
        scheduleOperation(Operation.Write);
    }

    protected void scheduleErase() {
        Log.d(TAG, "schedule erase operation");

        if (getConfiguration().getKeysFor("VAT") == null) {
            this.binding.writeOutcome.setText(R.string.message_keys_not_configured);
            return;
        }

        this.binding.writeOutcome.setText("");
        scheduleOperation(Operation.Erase);
    }

}