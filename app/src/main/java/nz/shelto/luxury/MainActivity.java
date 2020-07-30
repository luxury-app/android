package nz.shelto.luxury;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.shelto.luxury.fragments.CustomFragment;
import nz.shelto.luxury.fragments.HomeFragment;
import nz.shelto.luxury.fragments.OnFragmentInteractionListener;
import nz.shelto.luxury.fragments.SettingsFragment;
import nz.shelto.luxury.R;


public class MainActivity extends ActivityWrapper implements OnFragmentInteractionListener {


    private static int selectedTab = R.id.navigation_home;

    private Map<Integer, CustomFragment> fragmentMap = new HashMap<>();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            selectedTab = item.getItemId();
            applyFragment(fragmentMap.get(selectedTab), R.id.fragment);
            return true;
        }
    };

    private void applyFragment(final CustomFragment fragment, final int id) {
        final FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        fragment.setParentActivity(this);
        transaction.replace(id, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        final CustomFragment homeFragment = new HomeFragment();
        homeFragment.setParentActivity(this);
        fragmentMap.put(R.id.navigation_home,homeFragment);
        fragmentMap.put(R.id.navigation_settings, new SettingsFragment(this));
        final CustomFragment fragment = (CustomFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        if(fragment.getParentActivity() == null) {
            fragment.setParentActivity(this);
        }
        applyFragment(fragmentMap.get(selectedTab), R.id.fragment);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
