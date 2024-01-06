package de.dentrassi.vat.nfc.programmer.data;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

import de.dentrassi.vat.nfc.programmer.databinding.FragmentItemBinding;

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

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final CreatedCard item = this.entries.get(position);

        holder.chipUidView.setText(item.getUid());
        holder.memberIdView.setText(String.format(Locale.getDefault(), "%06d", item.getId().getMemberId()));
        holder.cardNumberView.setText(String.format(Locale.getDefault(), "%04d", item.getId().getCardNumber()));
        holder.timestampView.setText(item.getTimestamp()
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
        holder.cardUidView.setText(item.getId().getUid().toString());
    }

    @Override
    public int getItemCount() {
        return this.entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView chipUidView;
        public final TextView memberIdView;
        public final TextView cardNumberView;
        public final TextView timestampView;
        public final TextView cardUidView;

        public ViewHolder(final FragmentItemBinding binding) {
            super(binding.getRoot());

            this.chipUidView = binding.chipUid;
            this.memberIdView = binding.memberId;
            this.cardNumberView = binding.cardNumber;
            this.timestampView = binding.timestamp;
            this.cardUidView = binding.cardUid;
        }
    }
}