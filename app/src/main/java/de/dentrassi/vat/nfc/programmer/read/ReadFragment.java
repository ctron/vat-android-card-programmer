package de.dentrassi.vat.nfc.programmer.read;

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

import com.google.common.io.BaseEncoding;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import de.dentrassi.vat.nfc.programmer.MainActivity;
import de.dentrassi.vat.nfc.programmer.R;
import de.dentrassi.vat.nfc.programmer.config.Configuration;
import de.dentrassi.vat.nfc.programmer.databinding.ReadFragmentBinding;
import de.dentrassi.vat.nfc.programmer.model.CardId;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;
import de.dentrassi.vat.nfc.programmer.nfc.TagFragment;
import de.dentrassi.vat.nfc.programmer.nfc.action.ReadAction;
import de.dentrassi.vat.nfc.programmer.nfc.ops.Reader;

public class ReadFragment extends Fragment implements TagFragment {

    private static final String TAG = ReadFragment.class.getName();
    private Tag lateReadTag;

    private ReadFragmentBinding binding;

    public ReadFragment() {
    }

    public ReadFragment(final Tag lateReadTag) {
        this.lateReadTag = lateReadTag;
    }

    @Nullable
    @Override
    public View onCreateView(final @NonNull LayoutInflater inflater, final @Nullable ViewGroup container, final @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.read_fragment, container, false);
        this.binding = ReadFragmentBinding.bind(view);

        if (this.lateReadTag != null) {
            var tag = this.lateReadTag;
            this.lateReadTag = null;
            tagDiscovered(tag);
        }

        return view;
    }

    /**
     * Called when a tag was discovered, and we are in read mode.
     */
    public boolean tagDiscovered(@NonNull final Tag tag) {
        Log.d(TAG, "Tag discovered");

        final Keys keys = getConfiguration().getKeysFor("VAT");
        if (keys == null) {
            this.binding.infoCard.setVisibility(View.GONE);
            setTagText(getString(R.string.error_missing_configuration));
            return false;
        }

        new ReadAction(tag, keys, (id, ex) -> {
            Log.i(TAG, String.format("Read complete - id: %s", id), ex);

            if (ex instanceof ReadAction.UnauthorizedToReadException) {
                this.binding.infoCard.setVisibility(View.GONE);
                final StringBuilder sb = new StringBuilder(getString(R.string.message_either_wrong_encryption_keys_or_card_is_not_provisioned));
                if (isSupportedCard(tag)) {
                    sb.append(' ');
                    sb.append(getString(R.string.message_tag_can_be_used_for_access_control));
                }
                setTagText(sb.toString());
            } else if (ex instanceof Reader.RecordValidationFailed) {
                this.binding.infoCard.setVisibility(View.GONE);
                var err = ((Reader.RecordValidationFailed) ex);
                setTagText(String.format("Mismatch of provisioned card UID (record: %s, tag: %s)",
                        BaseEncoding.base16().encode(err.getCard()),
                        BaseEncoding.base16().encode(err.getTag())
                ));
            } else if (ex != null) {
                this.binding.infoCard.setVisibility(View.GONE);
                setTagText(String.format(getString(R.string.message_failed_to_read_tag), ex.getMessage()));
            } else if (id == null) {
                this.binding.infoCard.setVisibility(View.VISIBLE);
                tagReadEmpty(tag);
            } else {
                this.binding.infoCard.setVisibility(View.VISIBLE);
                tagRead(id);
            }
        }).run();

        return true;
    }

    private void tagReadEmpty(@NonNull final Tag tag) {
        this.binding.memberId.setText("n/a");
        this.binding.tagId.setText(BaseEncoding.base16().encode(tag.getId()));
        this.binding.infoText.setText("");
        setTagText("");

        if (isSupportedCard(tag)) {
            this.binding.infoText.setText(R.string.message_tag_can_be_used_for_access_control);
        }
    }

    private static boolean isSupportedCard(@NonNull final Tag tag) {
        if (tag.getTechList() != null) {
            if (Arrays.stream(tag.getTechList()).anyMatch(tech -> MifareClassic.class.getName().equals(tech))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called when a tag was read.
     */
    private void tagRead(@NonNull final CardId id) {
        this.binding.memberId.setText(String.format("%s", id.getMemberId()));
        this.binding.tagId.setText(BaseEncoding.base16().encode(id.getUid().getUid()));
        this.binding.infoText.setText("");
        setTagText("");
    }

    protected @NotNull Configuration getConfiguration() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getConfiguration();
        } else {
            return new Configuration();
        }
    }

    private void setTagText(@NonNull final String text) {
        this.binding.text.setText(text);
    }

}
