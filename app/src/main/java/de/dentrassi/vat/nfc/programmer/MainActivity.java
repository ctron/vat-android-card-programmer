package de.dentrassi.vat.nfc.programmer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;

import de.dentrassi.vat.nfc.programmer.config.ConfigFragment;
import de.dentrassi.vat.nfc.programmer.config.Configuration;
import de.dentrassi.vat.nfc.programmer.config.ConfigurationStore;
import de.dentrassi.vat.nfc.programmer.data.CreatedCard;
import de.dentrassi.vat.nfc.programmer.data.CreatedCardsContent;
import de.dentrassi.vat.nfc.programmer.data.ListFragment;
import de.dentrassi.vat.nfc.programmer.home.HomeFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NfcAdapter adapter;

    private HomeFragment mainTab;
    private ListFragment listTab;
    private ConfigFragment configTab;

    private CreatedCardsContent cards;
    private Configuration configuration = new Configuration();

    private final ActivityResultLauncher<String> importConfig = registerForActivityResult(new ActivityResultContracts.GetContent(), this::performImportConfig);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.cards = new CreatedCardsContent(getFilesDir().toPath());
        try {
            this.cards.load();
        } catch (final Exception e) {
            Log.w(TAG, "Failed to load cards", e);
        }

        try {
            this.configuration = ConfigurationStore.load(getConfigPath());
        } catch (final Exception e) {
            Log.w(TAG, "Failed to load configuration", e);
        }

        setContentView(R.layout.activity_main);

        final TabLayout tabLayout = findViewById(R.id.tabs);
        final ViewPager2 viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    default:
                    case 0: {
                        final HomeFragment result = new HomeFragment();
                        MainActivity.this.mainTab = result;
                        return result;
                    }
                    case 1: {
                        final ListFragment result = new ListFragment();
                        MainActivity.this.listTab = result;
                        return result;
                    }
                    case 2: {
                        final ConfigFragment result = new ConfigFragment();
                        MainActivity.this.configTab = result;
                        return result;
                    }
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        new TabLayoutMediator(
                tabLayout,
                viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(R.string.tab_home);
                            break;
                        case 1:
                            tab.setText(R.string.tab_data);
                            break;
                        case 2:
                            tab.setText(R.string.tab_config);
                            break;
                    }
                }
        )
                .attach();


        initNfc();
    }

    private @NotNull Path getConfigPath() {
        return getFilesDir().toPath().resolve("config.json");
    }

    private void initNfc() {
        final Object manager = getSystemService(Context.NFC_SERVICE);
        if (manager instanceof NfcManager) {
            this.adapter = ((NfcManager) manager).getDefaultAdapter();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatch();
    }

    @Override
    public void onPause() {
        disableForegroundDispatch();
        super.onPause();
    }

    private void enableForegroundDispatch() {
        if (this.adapter == null) {
            return;
        }

        final Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        final ArrayList<IntentFilter> f = new ArrayList<>();
        f.add(new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED));

        this.adapter.enableForegroundDispatch(this, pendingIntent, f.toArray(new IntentFilter[0]), null);
    }

    private void disableForegroundDispatch() {
        if (this.adapter != null) {
            this.adapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        Log.i(TAG, String.format("New Intent: %s", intent));

        if (this.mainTab != null && this.mainTab.isAdded()) {
            this.mainTab.onNewIntent(intent);
        }
    }

    void notifyCardsChange() {
        if (this.listTab != null && this.listTab.isAdded()) {
            this.listTab.refreshItems();
        }
    }

    public void addCard(@NonNull final CreatedCard card) {
        this.cards.add(card);
        cardsModified();
    }

    public void removeCard(@NonNull final String tagUid) {
        this.cards.remove(tagUid);
        cardsModified();
    }

    private void cardsModified() {
        try {
            this.cards.store();
        } catch (final Exception e) {
            Log.w(TAG, "Failed to store cards", e);
        }

        notifyCardsChange();
    }

    public @NonNull CreatedCardsContent getCards() {
        return this.cards;
    }

    public @NotNull Configuration getConfiguration() {
        return this.configuration;
    }

    public void importConfig() {
        this.importConfig.launch("application/json");
    }

    private void performImportConfig(final Uri uri) {
        if (uri == null) {
            Toast.makeText(this, R.string.message_import_cancelled, Toast.LENGTH_LONG).show();
            return;
        }

        try (final InputStream in = getContentResolver().openInputStream(uri)) {
            this.configuration = ConfigurationStore.load(in);
            this.configuration.store(getConfigPath());
            Toast.makeText(this, R.string.message_configuration_imported, Toast.LENGTH_SHORT).show();
            this.configTab.configChanged();
            this.mainTab.configChanged();
        } catch (final Exception e) {
            Log.w(TAG, "Failed to import configuration", e);
            Toast.makeText(this, String.format("Import failed: " + e), Toast.LENGTH_LONG).show();
        }
    }
}