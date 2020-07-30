package nz.shelto.luxury.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import nz.shelto.luxury.MeasureActivity;
import nz.shelto.luxury.R;
import nz.shelto.luxury.ResultActivity;

@SuppressLint("ValidFragment")
public class HomeFragment extends CustomFragment {



    public HomeFragment() {
        super(null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        this.parentActivity = this.getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        final FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(HomeFragment.this.getContext(), MeasureActivity.class);
                HomeFragment.this.startActivity(intent);
            }
        });
        return view;
    }
}
