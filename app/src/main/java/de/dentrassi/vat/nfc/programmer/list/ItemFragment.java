package de.dentrassi.vat.nfc.programmer.list;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.dentrassi.vat.nfc.programmer.MainActivity;
import de.dentrassi.vat.nfc.programmer.R;

/**
 * A fragment representing a list of Items.
 */
public class ItemFragment extends Fragment {

    private int columnCount = 1;

    private CreatedCardsContent cards;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            this.cards = ((MainActivity) getActivity()).getCards();
        }

    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (this.columnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, this.columnCount));
            }
            recyclerView.setAdapter(new CreatedCardRecyclerViewAdapter(this.cards.getEntries()));
        }
        return view;
    }

    public void refreshItems() {
        final RecyclerView recyclerView = (RecyclerView) getView();
        recyclerView.getAdapter().notifyDataSetChanged();
    }
}