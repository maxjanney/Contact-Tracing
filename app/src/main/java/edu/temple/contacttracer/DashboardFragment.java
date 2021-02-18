package edu.temple.contacttracer;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DashboardFragment extends Fragment {

    Dashboard dashboardActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Dashboard) {
            dashboardActivity = (Dashboard) context;
        } else {
            throw new RuntimeException("Must implement the Dashboard interface.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        root.findViewById(R.id.start_button).setOnClickListener(v -> dashboardActivity.start());
        root.findViewById(R.id.stop_button).setOnClickListener(v -> dashboardActivity.stop());
        return root;
    }

    interface Dashboard {
        void start();
        void stop();
    }
}