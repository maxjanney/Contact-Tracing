package edu.temple.contacttracer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

public class TraceFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap googleMap;
    MapView mapView;
    TextView dateText;
    Marker marker;

    Date date;
    LatLng point;

    public static TraceFragment newInstance(LatLng location, Date date) {
        TraceFragment fragment = new TraceFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("point", location);
        bundle.putSerializable("date", date);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle;
        if ((bundle = getArguments()) != null) {
            point = bundle.getParcelable("point");
            date = (Date) bundle.getSerializable("date");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trace, container, false);
        dateText = root.findViewById(R.id.dateText);
        MapsInitializer.initialize(getActivity());
        mapView = root.findViewById(R.id.map_view);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (point != null && date != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 20));
            marker = googleMap.addMarker((new MarkerOptions()).position(point));
            dateText.setText(date.toString());
        }
    }

}
