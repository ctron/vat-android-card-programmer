package de.dentrassi.vat.nfc.programmer.config;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import de.dentrassi.vat.nfc.programmer.MainActivity;
import de.dentrassi.vat.nfc.programmer.R;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;

public class ConfigTab extends Fragment {

    private TextView vatKeyA;
    private TextView vatKeyB;

    public ConfigTab() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.config, container, false);

        final Button importButton = view.findViewById(R.id.importConfig);
        importButton.setOnClickListener(this::onImportConfig);

        this.vatKeyA = view.findViewById(R.id.vatKeyA);
        this.vatKeyB = view.findViewById(R.id.vatKeyB);

        configChanged();

        return view;
    }

    protected void onImportConfig(final View view) {
        final FragmentActivity activity = getActivity();

        if (activity instanceof MainActivity) {
            ((MainActivity) activity).importConfig();
        }
    }

    public void configChanged() {
        final FragmentActivity activity = getActivity();

        if (activity instanceof MainActivity) {
            setConfig(((MainActivity) activity).getConfiguration());
        }
    }

    private void setConfig(@NonNull final Configuration configuration) {

        final Keys keys = configuration.getKeys().get("VAT");
        if (keys == null) {
            this.vatKeyA.setText(R.string.unset);
            this.vatKeyB.setText(R.string.unset);
        } else {
            this.vatKeyA.setText(keys.getA().toString());
            this.vatKeyB.setText(keys.getB().toString());
        }

    }

}
