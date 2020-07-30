package nz.shelto.luxury;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import nz.shelto.luxury.fragments.OnFragmentInteractionListener;
import nz.shelto.luxury.setup.DefaultSettingsFragment;
import nz.shelto.luxury.R;
import nz.shelto.luxury.setup.HowToFragment;

public class SetupActivity extends ActivityWrapper implements OnFragmentInteractionListener {

    private static int currentLocation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setup);
        final Button nextButton = findViewById(R.id.nextButton);
        final List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new HowToFragment(this));
        fragmentList.add(new DefaultSettingsFragment(this));
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLocation >= fragmentList.size()) {
                    final SharedPreferences preferences = SetupActivity.this.getSharedPreferences("default_prefs", 0);
                    preferences.edit().putBoolean("completedSetup", true).apply();
                    final Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(intent);
                    return;
                }
                if(currentLocation < fragmentList.size()) {
                    final Fragment currentFragment = fragmentList.get(currentLocation);
                    final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment, currentFragment);
                    transaction.addToBackStack(null);
                    currentLocation++;
                    transaction.commit();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(currentLocation > 0) {
            currentLocation--;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
