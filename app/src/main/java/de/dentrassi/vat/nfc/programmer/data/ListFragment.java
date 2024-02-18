package de.dentrassi.vat.nfc.programmer.data;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import de.dentrassi.vat.nfc.programmer.MainActivity;
import de.dentrassi.vat.nfc.programmer.R;

public class ListFragment extends Fragment {

    private static final String TAG = "ListFragment";

    private static final boolean ALLOW_CLEAR = false;

    private RecyclerView recyclerView;

    public ListFragment() {
        if (ALLOW_CLEAR) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState
    ) {
        final View view = inflater.inflate(R.layout.data_fragment_list, container, false);
        this.recyclerView = view.findViewById(R.id.cardList);

        // action button

        final FloatingActionButton fab = view.findViewById(R.id.shareCards);
        fab.setOnClickListener(x -> shareData());

        // list view

        final Context context = view.getContext();
        this.recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // setup data

        final List<CardEntry> cards;
        if (getActivity() instanceof MainActivity) {
            cards = ((MainActivity) getActivity()).getCards().getEntries();
        } else {
            // this should not happen, but does when changing the orientation
            cards = new LinkedList<>();
            Log.w(TAG, "Missing card information");
        }

        this.recyclerView.setAdapter(new CreatedCardRecyclerViewAdapter(context, cards));

        // make them look like list items

        var divider = new MaterialDividerItemDecoration(this.recyclerView.getContext(), MaterialDividerItemDecoration.VERTICAL);
        this.recyclerView.addItemDecoration(divider);

        // done

        return view;
    }

    @Override
    public void onCreateOptionsMenu(final @NonNull Menu menu, final @NonNull MenuInflater inflater) {
        if (ALLOW_CLEAR) {
            inflater.inflate(R.menu.data, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cards) {
            clearData();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void clearData() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_clearing_data)
                .setMessage(R.string.message_clearing_data)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    performClearData();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                })
                .create()
                .show();
    }

    private void performClearData() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getCards().clear();
            refreshItems();
        }
    }

    public void refreshItems() {
        this.recyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * Start a share intent to allow exporting the data
     */
    void shareData() {

        final Context context = getContext();
        if (!((getActivity() instanceof MainActivity) && context != null)) {
            return;
        }

        final CreatedCardStorage storage = ((MainActivity) getActivity()).getCardStorage();

        final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        final String name = "cards-" + timestamp + ".csv";

        final Uri uri = CsvFileProvider.getUriForFile(
                context,
                "de.dentrassi.vat.nfc.programmer.csvProvider", // must align with manifest XML
                storage.getPath().toFile(),
                name);

        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_export_of_cards));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        final Intent shareIntent = Intent.createChooser(intent, null);
        startActivity(shareIntent);
    }
}