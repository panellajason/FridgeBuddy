package com.example.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.app.R;
import com.example.app.models.Item;
import com.example.app.models.ItemAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FilterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button filterBtn;
    private EditText searchET;
    private Spinner spinner;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference itemRef = db.collection("Items");
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Search");

        filterBtn = findViewById(R.id.filterBtn);
        searchET = findViewById(R.id.searchET);
        spinner = findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.filters, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(this);

        stringSearch("","");

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stringSearch(searchET.getText().toString(), spinner.getSelectedItem().toString());
            }
        });
    }

    private void stringSearch (String search, String spinner) {
        Query query;
        if(spinner.trim().isEmpty() && search.trim().isEmpty())
            query = itemRef.orderBy("expTimestamp", Query.Direction.ASCENDING).whereEqualTo("userID", mAuth.getUid());
        else if(spinner.trim().isEmpty())
            query = itemRef.orderBy("name", Query.Direction.ASCENDING).startAt(search).endAt(search + "\uf8ff").whereEqualTo("userID", mAuth.getUid());
        else if(search.trim().isEmpty())
            query = itemRef.orderBy("expTimestamp", Query.Direction.ASCENDING).whereEqualTo("userID", mAuth.getUid()).whereEqualTo("storagelocation", spinner);
        else
            query = itemRef.orderBy("name", Query.Direction.ASCENDING).whereEqualTo("userID", mAuth.getUid()).whereEqualTo("storagelocation", spinner).startAt(search).endAt(search + "\uf8ff");


        FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        itemAdapter = new ItemAdapter(options);
        itemAdapter.startListening();

        RecyclerView recView = findViewById(R.id.recycler_viewFilter);
        recView.setHasFixedSize(true);
        recView.setLayoutManager(new LinearLayoutManager(this));
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
                String createdAt = documentSnapshot.toObject(Item.class).getCreatedAt().toDate().toString();

                Intent i = new Intent(FilterActivity.this, EditActivity.class);

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
    }

    @Override
    public void onStop() {
        super.onStop();
        itemAdapter.stopListening();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        stringSearch(searchET.getText().toString(), parent.getItemAtPosition(position).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_refresh:
                searchET.setText("");
                spinner.setSelection(0);
                stringSearch("","");
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

