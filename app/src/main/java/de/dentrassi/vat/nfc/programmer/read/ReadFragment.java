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
            this.tagDiscovered(tag);
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
            Log.i(TAG, String.format("Read complete - id: %s, ex: %s", id, ex));

            if (ex instanceof ReadAction.UnableToReadException) {
                this.binding.infoCard.setVisibility(View.GONE);
                setTagText(getString(R.string.message_either_wrong_encryption_keys_or_card_is_not_provisioned));
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

    private void tagReadEmpty(Tag tag) {
        this.binding.memberId.setText("n/a");
        this.binding.tagId.setText(BaseEncoding.base16().encode(tag.getId()));
        this.binding.infoText.setText("");
        setTagText("");

        if (tag.getTechList() != null) {
            if (Arrays.stream(tag.getTechList()).anyMatch(tech -> MifareClassic.class.getName().equals(tech))) {
                this.binding.infoText.setText(R.string.message_tag_can_be_used_for_access_control);
            }
        }
    }

    /**
     * Called when a tag was read.
     */
    private void tagRead(@NonNull final CardId id) {
        this.binding.memberId.setText(String.format("%s", id.getMemberId()));
        this.binding.tagId.setText(BaseEncoding.base16().encode(id.getUid()));
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
