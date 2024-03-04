package de.dentrassi.vat.nfc.programmer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.common.io.ByteStreams;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;

import de.dentrassi.vat.nfc.programmer.config.ConfigFragment;
import de.dentrassi.vat.nfc.programmer.config.Configuration;
import de.dentrassi.vat.nfc.programmer.config.ConfigurationStore;
import de.dentrassi.vat.nfc.programmer.data.CardEntry;
import de.dentrassi.vat.nfc.programmer.data.CreatedCardStorage;
import de.dentrassi.vat.nfc.programmer.data.CreatedCardsContent;
import de.dentrassi.vat.nfc.programmer.data.ListFragment;
import de.dentrassi.vat.nfc.programmer.home.HomeFragment;
import de.dentrassi.vat.nfc.programmer.home.Prefill;
import de.dentrassi.vat.nfc.programmer.model.Uid;
import de.dentrassi.vat.nfc.programmer.read.ReadFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    enum Tabs {
        Home, Read, Data, Config,
    }

    private final ActivityResultLauncher<String> importConfig = registerForActivityResult(new ActivityResultContracts.GetContent(), this::performImportConfig);

    private NfcAdapter adapter;

    private HomeFragment mainTab;
    private ReadFragment readTab;
    private ListFragment listTab;
    private ConfigFragment configTab;

    private CreatedCardStorage cardStorage;
    private CreatedCardsContent cards;
    private Configuration configuration = new Configuration();

    private CoordinatorLayout coordinatorLayout;
    private Snackbar snackbar;

    private Tag lateReadTag;
    private Prefill latePrefill;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.cardStorage = new CreatedCardStorage(getFilesDir().toPath());
        this.cards = new CreatedCardsContent();
        try {
            this.cards.load(this.cardStorage);
        } catch (final Exception e) {
            Log.w(TAG, "Failed to load cards", e);
        }

        try {
            this.configuration = ConfigurationStore.load(getConfigPath());
        } catch (final Exception e) {
            Log.w(TAG, "Failed to load configuration", e);
        }

        setContentView(R.layout.activity_main);

        this.coordinatorLayout = findViewById(R.id.coordinatorLayout);
        final TabLayout tabLayout = findViewById(R.id.tabs);
        final ViewPager2 viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                var tab = Tabs.values()[position];

                switch (tab) {
                    default:
                    case Home: {
                        final HomeFragment result = new HomeFragment(MainActivity.this.latePrefill);
                        MainActivity.this.mainTab = result;
                        return result;
                    }
                    case Read: {
                        final ReadFragment result = new ReadFragment(MainActivity.this.lateReadTag);
                        MainActivity.this.readTab = result;
                        return result;
                    }
                    case Data: {
                        final ListFragment result = new ListFragment();
                        MainActivity.this.listTab = result;
                        return result;
                    }
                    case Config: {
                        final ConfigFragment result = new ConfigFragment();
                        MainActivity.this.configTab = result;
                        return result;
                    }
                }
            }

            @Override
            public int getItemCount() {
                return Tabs.values().length;
            }
        });

        new TabLayoutMediator(
                tabLayout,
                viewPager,
                (tabView, position) -> {
                    var tab = Tabs.values()[position];
                    switch (tab) {
                        case Home:
                            tabView.setText(R.string.tab_home);
                            break;
                        case Read:
                            tabView.setText(R.string.tab_read);
                            break;
                        case Data:
                            tabView.setText(R.string.tab_data);
                            break;
                        case Config:
                            tabView.setText(R.string.tab_config);
                            break;
                    }
                }
        )
                .attach();

        this.snackbar = Snackbar.make(this.coordinatorLayout, "Configuration missing", Snackbar.LENGTH_INDEFINITE)
                .setBehavior(new BaseTransientBottomBar.Behavior() {
                    @Override
                    public boolean canSwipeDismissView(View child) {
                        return false;
                    }
                })
                .setAction(R.string.action_import, v -> setCurrentTab(Tabs.Config));
        checkConfiguration();

        initNfc();

        handleIntent(getIntent());
    }

    private void checkConfiguration() {
        if (!hasConfiguration()) {
            snackbar.show();
        } else {
            snackbar.dismiss();
        }
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

        handleIntent(intent);
    }

    @SuppressWarnings("deprecation")
    void handleIntent(@NonNull final Intent intent) {

        Log.i(TAG, String.format("Handle Intent: %s", intent));
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
            case NfcAdapter.ACTION_TAG_DISCOVERED: {
                this.lateReadTag = null;

                final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if (tag == null) {
                    Log.w(TAG, "Tag discovered, but provided no instance");
                    return;
                }

                var handled = false;
                if (this.mainTab != null && this.mainTab.isAdded()) {
                    handled = this.mainTab.tagDiscovered(tag);
                    Log.d(TAG, "main tab handled: " + handled);
                }
                if (!handled) {
                    if (this.readTab != null && this.readTab.isAdded()) {
                        handled = this.readTab.tagDiscovered(tag);
                        Log.d(TAG, "read tab handled: " + handled);
                    } else {
                        // record that state
                        this.lateReadTag = tag;
                    }
                    setCurrentTab(Tabs.Read);
                }
                break;
            }
            case Intent.ACTION_VIEW: {
                var data = intent.getData();
                Log.i(TAG, String.format("View data: %s", data));
                if (data == null) {
                    return;
                }
                var host = data.getHost();
                if (host == null) {
                    return;
                }
                switch (host) {
                    case "write": {
                        prefillWrite(data);
                        break;
                    }
                }

                break;
            }
        }

    }

    /**
     * Prefill the write fragment
     *
     * @param data the data from the intent
     */
    private void prefillWrite(final Uri data) {
        var prefill = Prefill.of(data);

        if (this.mainTab != null && this.mainTab.isAdded()) {
            this.latePrefill = null;
            this.mainTab.prefill(prefill);
        } else {
            // record that state
            this.latePrefill = prefill;
        }

        setCurrentTab(Tabs.Home);
    }

    void notifyCardsChange() {
        if (this.listTab != null && this.listTab.isAdded()) {
            this.listTab.refreshItems();
        }
    }

    public void addCard(@NonNull final CardEntry card) {
        this.cards.add(card);
        cardsModified();
    }

    public void removeCard(@NonNull final Uid uid) {
        this.cards.erase(uid);
        cardsModified();
    }

    private void cardsModified() {
        try {
            this.cards.store(this.cardStorage);
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

    public boolean hasConfiguration() {
        return this.configuration != null && this.configuration.getKeysFor("VAT") != null;
    }

    public void importConfig(final String password) {
        final SharedPreferences prefs = getSharedPreferences("ImportPassword", Context.MODE_PRIVATE);

        final SharedPreferences.Editor edit = prefs.edit();
        if (password != null) {
            edit.putString("password", password);
        } else {
            edit.remove("password");
        }
        edit.apply();

        this.importConfig.launch("*/*");
    }

    private void performImportConfig(final Uri uri) {

        final SharedPreferences prefs = getSharedPreferences("ImportPassword", Context.MODE_PRIVATE);
        final String password = prefs.getString("password", null);
        prefs.edit().clear().apply();

        if (uri == null) {
            Toast.makeText(this, R.string.message_import_cancelled, Toast.LENGTH_LONG).show();
            return;
        }

        try (final InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) {
                Toast.makeText(this, R.string.message_unable_to_open_selected_file, Toast.LENGTH_LONG).show();
                return;
            }

            if (password != null && !password.isEmpty()) {
                Log.d(TAG, "Loading config with password");
                final String data = new String(ByteStreams.toByteArray(in), StandardCharsets.UTF_8);
                this.configuration = ConfigurationStore.load(new ByteArrayInputStream(ConfigurationStore.decryptFromOpenSsl(data, password)));
            } else {
                Log.d(TAG, "Loading plain config");
                this.configuration = ConfigurationStore.load(in);
            }

            this.configuration.store(getConfigPath());

            Toast.makeText(this, R.string.message_configuration_imported, Toast.LENGTH_SHORT).show();
            this.configTab.configChanged();
            this.mainTab.configChanged();
        } catch (final Exception e) {
            Log.w(TAG, "Failed to import configuration", e);

            if (e instanceof BadPaddingException) {
                Toast.makeText(this, String.format(getString(R.string.error_unable_to_decode_configuration), e.getMessage()), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, String.format(getString(R.string.error_import_failed), e.getMessage()), Toast.LENGTH_LONG).show();
            }
        }

        checkConfiguration();

    }

    /**
     * Get the current tab.
     *
     * @return The current tab, falling back to {@link Tabs#Home} in case of errors.
     */
    private Tabs getCurrentTab() {
        final ViewPager2 viewPager = findViewById(R.id.view_pager);
        try {
            return Tabs.values()[viewPager.getCurrentItem()];
        } catch (final Exception e) {
            return Tabs.Home;
        }
    }

    private void setCurrentTab(@NonNull final Tabs tab) {
        final ViewPager2 viewPager = findViewById(R.id.view_pager);
        viewPager.setCurrentItem(tab.ordinal());
    }


    public CreatedCardStorage getCardStorage() {
        return this.cardStorage;
    }

}