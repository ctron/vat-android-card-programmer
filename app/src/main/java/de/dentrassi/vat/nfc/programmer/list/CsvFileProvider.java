package de.dentrassi.vat.nfc.programmer.list;

import androidx.core.content.FileProvider;

import de.dentrassi.vat.nfc.programmer.R;

public class CsvFileProvider extends FileProvider {
    public CsvFileProvider() {
        super(R.xml.provider_paths);
    }
}
