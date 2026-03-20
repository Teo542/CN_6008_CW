package com.cityfix.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cityfix.R;
import com.cityfix.models.FaultReport;
import com.cityfix.viewmodels.MapViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private MapViewModel viewModel;

    // Default: Athens, Greece
    private static final LatLng DEFAULT_LOCATION = new LatLng(37.9838, 23.7275);
    private static final float DEFAULT_ZOOM = 13f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MapViewModel.class);

        FloatingActionButton fabSubmit = view.findViewById(R.id.fab_submit);
        fabSubmit.setOnClickListener(v -> openSubmitReport());

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));

        // Observe LiveData — markers update automatically whenever Firestore changes
        viewModel.reports.observe(getViewLifecycleOwner(), this::updateMarkers);
    }

    private void updateMarkers(List<FaultReport> reports) {
        if (googleMap == null) return;
        googleMap.clear();

        for (FaultReport report : reports) {
            LatLng position = new LatLng(report.getLatitude(), report.getLongitude());
            float markerColor = getMarkerColor(report.getStatus());

            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(report.getTitle())
                    .snippet(report.getCategory() + " — " + report.getStatus())
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
        }
    }

    private float getMarkerColor(String status) {
        switch (status) {
            case "in_progress": return BitmapDescriptorFactory.HUE_ORANGE;
            case "resolved":    return BitmapDescriptorFactory.HUE_GREEN;
            default:            return BitmapDescriptorFactory.HUE_RED; // open
        }
    }

    private void openSubmitReport() {
        new SubmitReportFragment().show(getChildFragmentManager(), "submit_report");
    }
}
