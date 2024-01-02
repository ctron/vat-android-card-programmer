package de.dentrassi.vat.nfc.programmer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.IOException;
import java.util.ArrayList;

import de.dentrassi.vat.nfc.programmer.list.CreatedCard;
import de.dentrassi.vat.nfc.programmer.list.CreatedCardsContent;
import de.dentrassi.vat.nfc.programmer.list.ItemFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NfcAdapter adapter;

    private MainTab mainTab;
    private ItemFragment listTab;

    private CreatedCardsContent cards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.cards = new CreatedCardsContent(getFilesDir().toPath().resolve("cards.csv"));
        try {
            this.cards.load();
        } catch (final Exception e) {
            Log.w(TAG, "Failed to load cards", e);
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
                        final MainTab result = new MainTab();
                        MainActivity.this.mainTab = result;
                        return result;
                    }
                    case 1: {
                        final ItemFragment result = new ItemFragment();
                        MainActivity.this.listTab = result;
                        return result;
                    }
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        new TabLayoutMediator(
                tabLayout,
                viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Tag");
                            break;
                        case 1:
                            tab.setText("Data");
                            break;
                    }
                }
        )
                .attach();


        initNfc();
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

    void addCard(@NonNull final CreatedCard card) {
        this.cards.add(card);

        try {
            this.cards.store();
        } catch (IOException e) {
            Log.w(TAG, "Failed to store cards", e);
        }

        notifyCardsChange();
    }

    public CreatedCardsContent getCards() {
        return this.cards;
    }
}