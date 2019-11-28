package com.example.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app.R;
import com.example.app.activities.EditActivity;
import com.example.app.models.Item;
import com.example.app.models.ItemAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ListFragment extends Fragment {

    public static final String EXP_TIMESTAMP = "expTimestamp";
    public static final String USER_ID = "userID";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference itemRef = db.collection("Items");
    private ItemAdapter itemAdapter;

    public ListFragment() {}

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_list, container, false);
        getActivity().setTitle("Your Food List");
        setUpRecyclerView(view);

        return view;
    }

    private void setUpRecyclerView(final View view) {

        final Query query = itemRef.orderBy(EXP_TIMESTAMP, Query.Direction.ASCENDING).whereEqualTo(USER_ID, mAuth.getUid())
                .whereGreaterThan(EXP_TIMESTAMP, Timestamp.now());

        final FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        itemAdapter = new ItemAdapter(options);

        final RecyclerView recView = view.findViewById(R.id.recycler_view);
        recView.setHasFixedSize(true);
        recView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recView.setAdapter(itemAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull final RecyclerView recyclerView, @NonNull final RecyclerView.ViewHolder viewHolder,
                                  @NonNull final RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, final int direction) {
                itemAdapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recView);

        itemAdapter.setOnItemClickListener(new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final DocumentSnapshot documentSnapshot, final int position) {

                bundleAndSendData(documentSnapshot);
            }
        });

    }

    private void bundleAndSendData(final DocumentSnapshot documentSnapshot) {
        final String createdAt = documentSnapshot.toObject(Item.class).getCreatedAt().toDate().toString();
        final String expDate = documentSnapshot.toObject(Item.class).getExpdate();
        final String name = documentSnapshot.toObject(Item.class).getName();
        final String path = documentSnapshot.getReference().getPath();
        final String storageLocation = documentSnapshot.toObject(Item.class).getStoragelocation();

        final Intent i = new Intent(getActivity(), EditActivity.class);
        i.putExtra("CREATED_AT", createdAt);
        i.putExtra("EXP_DATE", expDate);
        i.putExtra("NAME", name);
        i.putExtra("PATH", path);
        i.putExtra("STORAGE_LOCATION", storageLocation);

        startActivity(i);
    }

    @Override
    public void onStart() {
        super.onStart();
        itemAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        itemAdapter.stopListening();
    }
}
