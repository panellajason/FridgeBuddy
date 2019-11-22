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
import android.widget.TextView;

import com.example.app.R;
import com.example.app.activities.EditActivity;
import com.example.app.activities.MainActivity;
import com.example.app.models.AlmostExpItemAdapter;
import com.example.app.models.ExpiredItemAdapter;
import com.example.app.models.Item;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class NotificationsFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference itemRef = db.collection("Items");
    private ExpiredItemAdapter itemAdapter;
    private AlmostExpItemAdapter itemAdapter2;
    private TextView expiringTV;
    private TextView changeThisTV;
    private Calendar cal = new GregorianCalendar();

    public NotificationsFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        getActivity().setTitle("Notifications");
        expiringTV = view.findViewById(R.id.expiringTV);
        changeThisTV = view.findViewById(R.id.changeThisTV);

        expiringTV.setText("Expiring within " + MainActivity.settings + " day(s):");

        changeThisTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).goToAccount();

            }
        });
        setUpRecyclerView(view);
        setupRecyclerView2(view);


        return view;
    }

    private void setUpRecyclerView(View view) {

        Query query = itemRef.orderBy("expTimestamp", Query.Direction.ASCENDING).whereEqualTo("userID", mAuth.getUid())
                .whereLessThan("expTimestamp", Timestamp.now());

        FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        itemAdapter = new ExpiredItemAdapter(options);

        RecyclerView recView = view.findViewById(R.id.recycler_viewNotifications);
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

        itemAdapter.setOnItemClickListener(new ExpiredItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                String path = documentSnapshot.getReference().getPath();
                String name = documentSnapshot.toObject(Item.class).getName();
                String expDate = documentSnapshot.toObject(Item.class).getExpdate();
                String storageLocation = documentSnapshot.toObject(Item.class).getStoragelocation();
                String createdAt = documentSnapshot.toObject(Item.class).getCreatedAt().toDate().toString();

                Intent i = new Intent(getActivity(), EditActivity.class);

                i.putExtra("PATH", path);
                i.putExtra("NAME", name);
                i.putExtra("EXP_DATE", expDate);
                i.putExtra("STORAGE_LOCATION", storageLocation);
                i.putExtra("CREATED_AT", createdAt);

                startActivity(i);
            }
        });

    }

    private void setupRecyclerView2(View view) {

        cal.add(Calendar.DAY_OF_MONTH, MainActivity.settings);
        Date date = cal.getTime();

        Query query = itemRef.orderBy("expTimestamp", Query.Direction.ASCENDING).whereEqualTo("userID", mAuth.getUid())
                .whereGreaterThan("expTimestamp", Timestamp.now())
                .whereLessThan("expTimestamp", new Timestamp(date));


        FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        itemAdapter2 = new AlmostExpItemAdapter(options);

        RecyclerView recView = view.findViewById(R.id.recycler_viewNotifications2);
        recView.setHasFixedSize(true);
        recView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recView.setAdapter(itemAdapter2);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                itemAdapter2.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recView);

        itemAdapter2.setOnItemClickListener(new AlmostExpItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                String path = documentSnapshot.getReference().getPath();
                String name = documentSnapshot.toObject(Item.class).getName();
                String expDate = documentSnapshot.toObject(Item.class).getExpdate();
                String storageLocation = documentSnapshot.toObject(Item.class).getStoragelocation();
                String createdAt = documentSnapshot.toObject(Item.class).getCreatedAt().toDate().toString();

                Intent i = new Intent(getActivity(), EditActivity.class);

                i.putExtra("PATH", path);
                i.putExtra("NAME", name);
                i.putExtra("EXP_DATE", expDate);
                i.putExtra("STORAGE_LOCATION", storageLocation);
                i.putExtra("CREATED_AT", createdAt);

                startActivity(i);
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        itemAdapter.startListening();
        itemAdapter2.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        itemAdapter.stopListening();
        itemAdapter2.stopListening();

    }
}
