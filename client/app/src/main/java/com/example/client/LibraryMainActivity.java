package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LibraryMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // 隐藏标题

        ImageButton profileButton = findViewById(R.id.profile_button);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LibraryMainActivity.this, StudentProfileActivity.class);
                startActivity(intent);
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_search) {
                selectedFragment = new BookSearchFragment();
            } else if (itemId == R.id.nav_seat) {
                selectedFragment = new SeatReservationFragment();
            } else if (itemId == R.id.nav_stats) {
                selectedFragment = new StudyStatsFragment();
            } else if (itemId == R.id.nav_ai) {
                selectedFragment = new AiFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // 默认加载图书检索界面
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BookSearchFragment())
                    .commit();
        }
    }
}