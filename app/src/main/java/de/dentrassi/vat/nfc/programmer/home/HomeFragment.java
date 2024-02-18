package de.dentrassi.vat.nfc.programmer.home;

import android.annotation.SuppressLint;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import de.dentrassi.vat.nfc.programmer.MainActivity;
import de.dentrassi.vat.nfc.programmer.R;
import de.dentrassi.vat.nfc.programmer.config.Configuration;
import de.dentrassi.vat.nfc.programmer.data.CardEntry;
import de.dentrassi.vat.nfc.programmer.databinding.HomeFragmentBinding;
import de.dentrassi.vat.nfc.programmer.model.AdditionalInformation;
import de.dentrassi.vat.nfc.programmer.model.IdType;
import de.dentrassi.vat.nfc.programmer.model.Uid;
import de.dentrassi.vat.nfc.programmer.model.WriteCardInformation;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.TagFragment;
import de.dentrassi.vat.nfc.programmer.nfc.action.EraseAction;
import de.dentrassi.vat.nfc.programmer.nfc.action.WriteAction;
import de.dentrassi.vat.nfc.programmer.utils.Editables;
import de.dentrassi.vat.nfc.programmer.utils.TextWatcherAdapter;
import de.dentrassi.vat.nfc.programmer.utils.validation.DriversLicense;
import de.dentrassi.vat.nfc.programmer.utils.validation.Error;
import de.dentrassi.vat.nfc.programmer.utils.validation.FormValidator;
import de.dentrassi.vat.nfc.programmer.utils.validation.Ok;
import de.dentrassi.vat.nfc.programmer.utils.validation.Result;
import de.dentrassi.vat.nfc.programmer.utils.validation.TextValidator;
import de.dentrassi.vat.nfc.programmer.utils.validation.Warning;

public class HomeFragment extends Fragment implements TagFragment {

    private static final String TAG = "MainTab";

    private final FormValidator validator = new FormValidator(this::validateInput, ok -> this.binding.startWriteButton.setEnabled(ok));

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

        this.validator.reset();

        this.binding.memberIdInput.setSelectAllOnFocus(true);
        this.binding.memberIdInput.addTextChangedListener(new TextValidator(this.binding.layoutMemberIdInput, validator) {
            @Override
            protected @NonNull Result validate(final String value) {

                if (value.isEmpty()) {
                    return Error.of(getString(R.string.validation_error_must_not_be_empty));
                }

                return Ok.of();
            }
        });

        this.binding.holderId.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                validator.validate();
            }
        });

        this.binding.holderIdType.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(final Editable e) {
                validator.validate();
            }
        });

        this.binding.startWriteButton.setOnClickListener(this::onScheduleWrite);
        this.binding.startEraseButton.setOnClickListener(this::onScheduleErase);
        this.binding.cancelOperationButton.setOnClickListener(this::onCancelOperation);
        if (getContext() != null) {
            this.binding.holderIdType.setText(getContext().getResources().getStringArray(R.array.id_types)[0], false);
        }

        validator.validate();
        configChanged();

        return view;
    }

    private Result validateInput() {
        final IdType type = IdType.fromLocalizedText(getContext(), this.binding.holderIdType.getText());
        this.binding.holderIdTypeWrapper.setEnabled(type != IdType.None);

        switch (type) {
            case CardNumber:
                return Result.runWith(this.binding.holderIdTypeWrapper, () -> {
                    var cardNumber = Editables.getText(this.binding.holderId.getText());

                    if (cardNumber.isEmpty()) {
                        return Error.of(getString(R.string.error_card_number_must_not_be_empty));
                    }

                    try {
                        Integer.parseInt(cardNumber);
                    } catch (Exception e) {
                        return Error.of(String.format(getString(R.string.error_card_number_must_be_a_number_was), cardNumber));
                    }

                    return Ok.of();
                });

            case DriversLicense:
                return Result.runWith(this.binding.holderIdTypeWrapper, () -> {

                    var cardNumber = Editables.getText(this.binding.holderId.getText());
                    if (cardNumber.isEmpty()) {
                        return Error.of(getString(R.string.error_drivers_license_must_not_be_empty));
                    }

                    if (!DriversLicense.isValidGermanLicenseNumber(cardNumber)) {
                        return Warning.of("Invalid driver license number");
                    }

                    return Ok.of();
                });

            case None:
            case Other:
                return Result.runWith(this.binding.holderIdTypeWrapper, Ok::of);
        }

        return Ok.of();
    }

    public void configChanged() {
        if (getConfiguration().getOrganizations().isEmpty()) {
            this.binding.warningMissingConfiguration.setVisibility(View.VISIBLE);
            this.binding.startWriteButton.setEnabled(false);
            this.binding.startEraseButton.setEnabled(false);
        } else {
            this.binding.warningMissingConfiguration.setVisibility(View.GONE);
            this.binding.startEraseButton.setEnabled(true);
            // re-validate for the startWriteButton
            this.validator.validate();
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
    public boolean tagDiscovered(@NonNull final Tag tag) {
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
                        return true;
                    }
                    new WriteAction(tag, keys, information, false, this::writeComplete).run();
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
                        return true;
                    }
                    new EraseAction(tag, keys, this::eraseComplete).run();
                } catch (final Exception e) {
                    Log.w(TAG, "Failed to erase tag", e);
                    setTagText(String.format("Failed to erase tag: %s", e.getMessage()));
                }
                break;
            }
            case None: {
                return false;
            }
        }

        return true;
    }

    /**
     * Set the current tag state.
     *
     * @param text the text to show
     */
    private void setTagText(final String text) {
        this.binding.writeOutcome.setText(text);
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

    @SuppressLint("StringFormatMatches")
    private void writeComplete(final @Nullable CardEntry result, @Nullable final Exception ex) {
        Log.d(TAG, String.format("writeComplete - result: %s, ex: %s", result, ex));

        if (ex instanceof WriteAction.AlreadyProvisioned) {
            var memberId = ((WriteAction.AlreadyProvisioned) ex).getId().getMemberId();
            this.binding.writeOutcome.setText(String.format(getString(R.string.error_already_provisioned), memberId));
        } else if (ex != null) {
            this.binding.writeOutcome.setText(String.format(getString(R.string.error_failed_to_write), ex.getMessage()));
        } else {
            this.binding.writeOutcome.setText(R.string.message_tag_written);
        }

        scheduleOperation(Operation.None);

        if ((result != null) && (getActivity() instanceof MainActivity)) {
            ((MainActivity) getActivity()).addCard(result);
        }
    }

    private void eraseComplete(@Nullable final Uid result, @Nullable final Exception ex) {
        Log.d(TAG, String.format("erase - result: %s, ex: %s", result == null ? "<null>" : result.toHex(), ex));

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