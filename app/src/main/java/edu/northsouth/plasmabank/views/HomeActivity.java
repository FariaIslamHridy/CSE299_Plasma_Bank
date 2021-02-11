package edu.northsouth.plasmabank.views;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import edu.northsouth.plasmabank.R;
import edu.northsouth.plasmabank.utils.CustomProgressBar;
import edu.northsouth.plasmabank.views.fragments.HomeFragment;
import edu.northsouth.plasmabank.views.fragments.NotificationsFragment;
import edu.northsouth.plasmabank.views.fragments.profile.ProfileFragment;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

    //    ImageButton mLogout;
    CustomProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_home);

        progressBar = new CustomProgressBar(this);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        fragmentManager = getSupportFragmentManager();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment fragment;

                switch (item.getItemId()) {
                    case R.id.navigation_notification:
                        fragment = NotificationsFragment.newInstance();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_home:
                        fragment = HomeFragment.newInstance();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_profile:
                        fragment = ProfileFragment.newInstance();
                        loadFragment(fragment);
                        return true;

                    default:
                        fragment = HomeFragment.newInstance();
                        loadFragment(fragment);
                        return true;
                }
            }
        });

        // load the store fragment by default
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        loadFragment(new HomeFragment());

    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.left_enter, R.anim.right_out);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            fragmentManager.popBackStack();

            if (count == 1) {
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, new HomeFragment())
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                                R.anim.left_enter, R.anim.right_out)
                        .commit();
            }

        }
    }
}