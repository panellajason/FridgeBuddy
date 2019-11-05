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
import com.example.app.fragments.AccountFragment;
import com.example.app.fragments.ListFragment;
import com.example.app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNav;
    private Fragment listFragment;
    private Fragment accountFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null) {
             listFragment = new ListFragment();
             accountFragment = new AccountFragment();

            bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);

            replaceFragment(listFragment);

            bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch (menuItem.getItemId()) {

                        case R.id.navList:
                            replaceFragment(listFragment);
                            return true;
                        case R.id.navAdd:
                            startActivity(new Intent(MainActivity.this, NewItemActivity.class));
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
            default:
                return false;
        }
    }

    private void logout() {
        mAuth.signOut();
        sendToLogin();
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
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction fragTras = getSupportFragmentManager().beginTransaction();
        fragTras.replace(R.id.mainContainer, fragment);
        fragTras.commit();
    }
}
