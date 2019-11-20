package com.example.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.app.fragments.AccountFragment;
import com.example.app.fragments.AddFragment;
import com.example.app.fragments.ListFragment;
import com.example.app.R;
import com.example.app.fragments.NotificationsFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


/*
    -recommendations for storage
    -timestamp for food item
    -notifications
    -shopping list (user can highlight item if they have it)
 */



public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private BottomNavigationView bottomNav;
    private Fragment listFragment;
    private Fragment accountFragment;
    private Fragment addFragment;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference userItemRef = db.document("User Settings/" + mAuth.getUid());
    public static int settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Food List");


        if(mAuth.getCurrentUser() != null) {

            userItemRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    if(documentSnapshot.exists()) {

                        settings = Integer.parseInt(documentSnapshot.getString("notification_settings"));

                    } else {
                        Toast.makeText(getApplicationContext(), "Document doesn't exist", Toast.LENGTH_SHORT).show();

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });


            listFragment = new ListFragment();
            accountFragment = new AccountFragment();
            addFragment = new AddFragment();

            bottomNav = findViewById(R.id.bottomNav);

            replaceFragment(listFragment);

            bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch (menuItem.getItemId()) {

                        case R.id.navList:
                            replaceFragment(listFragment);
                            return true;
                        case R.id.navAdd:
                            replaceFragment(addFragment);
                            return true;
                        case R.id.navNotifications:
                             replaceFragment(new NotificationsFragment());
                             return true;
                        case R.id.navAccount:
                           replaceFragment(accountFragment);
                            return true;

                        default:
                            return false;
                    }
                }
            });

            if (savedInstanceState == null) {
                bottomNav.setSelectedItemId(R.id.navList);
            }
        }
        else {
            sendToLogin();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.actionLogout:
                logout();
                return true;
            case R.id.actionSettings:
                replaceFragment(accountFragment);
                return true;
            case R.id.actionSearch:
                startActivity(new Intent(MainActivity.this, FilterActivity.class));
            default:
                return false;
        }
    }

    private void logout() {
        mAuth.signOut();
        sendToLogin();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null) {

            sendToLogin();
        }

        bottomNav.setSelectedItemId(R.id.navList);

    }

    private void sendToLogin() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction fragTras = getSupportFragmentManager().beginTransaction();
        fragTras.replace(R.id.mainContainer, fragment);
        fragTras.commit();
    }

}

