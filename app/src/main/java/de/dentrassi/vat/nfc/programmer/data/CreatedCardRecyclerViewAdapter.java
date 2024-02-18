package de.dentrassi.vat.nfc.programmer.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.io.BaseEncoding;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

import de.dentrassi.vat.nfc.programmer.R;
import de.dentrassi.vat.nfc.programmer.databinding.DataFragmentItemBinding;
import de.dentrassi.vat.nfc.programmer.model.IdType;

public class CreatedCardRecyclerViewAdapter extends RecyclerView.Adapter<CreatedCardRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private final List<CardEntry> entries;

    public CreatedCardRecyclerViewAdapter(@NonNull final Context context, @NonNull final List<CardEntry> items) {
        this.context = context;
        this.entries = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(
                DataFragmentItemBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final CardEntry item = this.entries.get(position);

        holder.uidView.setText(BaseEncoding.base16().encode(item.getId().getUid().getUid()));
        holder.memberIdView.setText(String.format(Locale.getDefault(), "%06d", item.getId().getMemberId()));
        holder.holderNameView.setText(item.getAdditional().getName());
        holder.holderIdView.setText(item.getAdditional().getId());

        if (item.getAdditional().getIdType() != IdType.None) {
            holder.holderIdTypeView.setText(this.context.getResources().getStringArray(R.array.id_types)[item.getAdditional().getIdType().ordinal()]);
        } else {
            holder.holderIdTypeView.setText("");
        }

        holder.timestampView.setText(item.getTimestamp()
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));

        if (item.isErased()) {
            holder.erasedMarkerView.setVisibility(View.VISIBLE);
        } else {
            holder.erasedMarkerView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return this.entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView uidView;
        public final TextView memberIdView;
        public final TextView holderNameView;
        public final TextView holderIdView;
        public final TextView holderIdTypeView;
        public final TextView timestampView;
        public final TextView erasedMarkerView;

        public ViewHolder(final DataFragmentItemBinding binding) {
            super(binding.getRoot());

            this.uidView = binding.uid;
            this.memberIdView = binding.memberId;
            this.holderNameView = binding.holderName;
            this.holderIdView = binding.holderId;
            this.holderIdTypeView = binding.holderIdType;
            this.timestampView = binding.timestamp;
            this.erasedMarkerView = binding.erasedMarker;
        }
    }
}