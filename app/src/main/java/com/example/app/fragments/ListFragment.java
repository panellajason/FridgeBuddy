package com.example.app.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.app.R;
import com.example.app.activities.EditActivity;
import com.example.app.models.Item;
import com.example.app.models.ItemAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class ListFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference itemRef = db.collection("Items");
    private ItemAdapter itemAdapter;

    public ListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);
        getActivity().setTitle("Your Food List");
        setUpRecyclerView(view);

        return view;
    }

    private void setUpRecyclerView(View view) {

        Query query = itemRef.orderBy("timestamp", Query.Direction.ASCENDING).whereEqualTo("userID", mAuth.getUid());


        FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        itemAdapter = new ItemAdapter(options);

        RecyclerView recView = view.findViewById(R.id.recycler_view);
        recView.setHasFixedSize(true);
        recView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recView.setAdapter(itemAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                itemAdapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recView);

        itemAdapter.setOnItemClickListener(new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                String path = documentSnapshot.getReference().getPath();
                String name = documentSnapshot.toObject(Item.class).getName();
                String expDate = documentSnapshot.toObject(Item.class).getExpdate();
                String storageLocation = documentSnapshot.toObject(Item.class).getStoragelocation();

                Intent i = new Intent(getActivity(), EditActivity.class);

                i.putExtra("PATH", path);
                i.putExtra("NAME", name);
                i.putExtra("EXP_DATE", expDate);
                i.putExtra("STORAGE_LOCATION", storageLocation);

                startActivity(i);
            }
        });

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
