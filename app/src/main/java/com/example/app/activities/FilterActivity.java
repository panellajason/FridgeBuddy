package com.example.app.activities;

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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app.R;
import com.example.app.models.Item;
import com.example.app.models.ItemAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FilterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String EXP_TIMESTAMP = "expTimestamp";
    public static final String USER_ID = "userID";
    public static final String NAME = "name";
    public static final String STORAGELOCATION = "storagelocation";
    public static final String CREATED_AT = "CREATED_AT";
    public static final String EXP_DATE = "EXP_DATE";
    public static final String PRODUCT_NAME = "NAME";
    public static final String PATH = "PATH";
    public static final String STORAGE_LOCATION = "STORAGE_LOCATION";
    private Button filterBtn;
    private EditText searchET;
    private Spinner spinner;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference itemRef = db.collection("Items");
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Search");

        filterBtn = findViewById(R.id.filterBtn);
        searchET = findViewById(R.id.searchET);
        spinner = findViewById(R.id.spinner);

        final ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.filters,
                android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(this);

        stringSearch("", "");

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                stringSearch(searchET.getText().toString(), spinner.getSelectedItem().toString());
            }
        });
    }

    private void stringSearch(final String search, final String spinner) {
        final Query query;
        if (spinner.trim().isEmpty() && search.trim().isEmpty())
            query = itemRef.orderBy(EXP_TIMESTAMP, Query.Direction.ASCENDING).whereEqualTo(USER_ID, mAuth.getUid());
        else if (spinner.trim().isEmpty())
            query = itemRef.orderBy(NAME, Query.Direction.ASCENDING).startAt(search).endAt(search + "\uf8ff").whereEqualTo(USER_ID,
                    mAuth.getUid());
        else if (search.trim().isEmpty())
            query = itemRef.orderBy(EXP_TIMESTAMP, Query.Direction.ASCENDING).whereEqualTo(USER_ID, mAuth.getUid()).whereEqualTo(
                    STORAGELOCATION, spinner);
        else
            query = itemRef.orderBy(NAME, Query.Direction.ASCENDING).whereEqualTo(USER_ID, mAuth.getUid()).whereEqualTo(
                    STORAGELOCATION, spinner).startAt(search).endAt(search + "\uf8ff");

        final FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        itemAdapter = new ItemAdapter(options);
        itemAdapter.startListening();

        final RecyclerView recView = findViewById(R.id.recycler_viewFilter);
        recView.setHasFixedSize(true);
        recView.setLayoutManager(new LinearLayoutManager(this));
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

        final Intent i = new Intent(this, EditActivity.class);
        i.putExtra(CREATED_AT, createdAt);
        i.putExtra(EXP_DATE, expDate);
        i.putExtra(PRODUCT_NAME, name);
        i.putExtra(PATH, path);
        i.putExtra(STORAGE_LOCATION, storageLocation);

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

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {

        stringSearch(searchET.getText().toString(), parent.getItemAtPosition(position).toString());
    }

    @Override
    public void onNothingSelected(final AdapterView<?> parent) {
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_refresh:
                searchET.setText("");
                spinner.setSelection(0);
                stringSearch("", "");
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

