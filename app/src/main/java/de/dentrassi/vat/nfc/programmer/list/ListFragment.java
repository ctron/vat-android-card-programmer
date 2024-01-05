package de.dentrassi.vat.nfc.programmer.list;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import de.dentrassi.vat.nfc.programmer.MainActivity;
import de.dentrassi.vat.nfc.programmer.R;

public class ListFragment extends Fragment {

    private static final String TAG = "ListFragment";

    private final int columnCount = 1;

    public ListFragment() {
    }

    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState
    ) {
        final View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.shareCards);
        fab.setOnClickListener(x -> {
            shareData();
        });

        final RecyclerView recyclerView = view.findViewById(R.id.cardList);

        final Context context = view.getContext();
        if (this.columnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, this.columnCount));
        }

        final List<CreatedCard> cards;
        if (getActivity() instanceof MainActivity) {
            cards = ((MainActivity) getActivity()).getCards().getEntries();
        } else {
            // this should not happen, but does when changing the orientation
            cards = new LinkedList<>();
            Log.w(TAG, "Missing card information");
        }

        recyclerView.setAdapter(new CreatedCardRecyclerViewAdapter(cards));

        return view;
    }

    public void refreshItems() {
        final View view = getView();
        if (view == null) {
            return;
        }

        final RecyclerView recyclerView = view.findViewById(R.id.cardList);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    void shareData() {

        final Context context = getContext();
        if (!((getActivity() instanceof MainActivity) && context != null)) {
            return;
        }

        final CreatedCardsContent cards = ((MainActivity) getActivity()).getCards();

        final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        final String name = "cards-" + timestamp + ".csv";

        final Uri uri = CsvFileProvider.getUriForFile(
                context,
                "de.dentrassi.vat.nfc.programmer.csvProvider", // must align with manifest XML
                cards.getPath().toFile(),
                name);

        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TITLE, "Export of cards");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        final Intent shareIntent = Intent.createChooser(intent, null);
        startActivity(shareIntent);
    }
}