package com.example.app.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class ExpiredItemAdapter extends FirestoreRecyclerAdapter<Item, ExpiredItemAdapter.ItemHolder> {

    private OnItemClickListener listener;

    public ExpiredItemAdapter(@NonNull final FirestoreRecyclerOptions<Item> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ItemHolder itemHolder, final int i, @NonNull final Item item) {
        itemHolder.name.setText(item.getName());
        itemHolder.expDate.setText("on " + item.getExpdate());
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {

        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.expired_item, parent, false);
        return new ItemHolder(v);
    }

    public void deleteItem(final int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView expDate;

        public ItemHolder(@NonNull final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.expired_item_name);
            expDate = itemView.findViewById(R.id.expired_date);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

}
