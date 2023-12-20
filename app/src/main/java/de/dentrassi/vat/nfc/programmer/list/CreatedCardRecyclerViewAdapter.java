package de.dentrassi.vat.nfc.programmer.list;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import de.dentrassi.vat.nfc.programmer.databinding.FragmentItemBinding;

/**
 * {@link RecyclerView.Adapter} that can display a {@link CreatedCard}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CreatedCardRecyclerViewAdapter extends RecyclerView.Adapter<CreatedCardRecyclerViewAdapter.ViewHolder> {

    private final List<CreatedCard> entries;

    public CreatedCardRecyclerViewAdapter(final List<CreatedCard> items) {
        this.entries = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(
                FragmentItemBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final CreatedCard item = this.entries.get(position);


        holder.uidView.setText(item.getUid());
        holder.memberIdView.setText(String.format(Locale.getDefault(), "%06d", item.getId().getMemberId()));
        holder.cardNumberView.setText(String.format(Locale.getDefault(), "%04d", item.getId().getCardNumber()));

        // holder.uidView.setText(String.format("%s %06d / %04d", item.getUid(), item.getId().getMemberId(), item.getId().getCardNumber()));
    }

    @Override
    public int getItemCount() {
        return this.entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView uidView;
        public final TextView memberIdView;
        public final TextView cardNumberView;

        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());

            this.uidView = binding.cardUid;
            this.memberIdView = binding.memberId;
            this.cardNumberView = binding.cardNumber;
        }
    }
}