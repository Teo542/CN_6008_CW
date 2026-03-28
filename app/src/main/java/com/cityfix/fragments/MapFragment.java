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
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders an interactive Google Map showing geolocated fault report markers.
 * Colour-codes markers by report status (red = open, orange = in-progress,
 * green = resolved) and supports chip-based filtering. A floating action button
 * opens {@link SubmitReportFragment} pre-populated with the current map centre
 * coordinates. Optionally accepts {@code focus_lat} / {@code focus_lng} arguments
 * to pan the camera to a specific report on load.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private MapViewModel viewModel;
    private List<FaultReport> allReports = new ArrayList<>();
    private String activeFilter = "all";

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

        setupFilterChips(view);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupFilterChips(View view) {
        Chip chipAll = view.findViewById(R.id.chip_all);
        Chip chipOpen = view.findViewById(R.id.chip_open);
        Chip chipInProgress = view.findViewById(R.id.chip_in_progress);
        Chip chipResolved = view.findViewById(R.id.chip_resolved);

        chipAll.setOnClickListener(v -> { activeFilter = "all"; applyFilter(); });
        chipOpen.setOnClickListener(v -> { activeFilter = "open"; applyFilter(); });
        chipInProgress.setOnClickListener(v -> { activeFilter = "in_progress"; applyFilter(); });
        chipResolved.setOnClickListener(v -> { activeFilter = "resolved"; applyFilter(); });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);

        android.os.Bundle args = getArguments();
        if (args != null && args.containsKey("focus_lat")) {
            LatLng focus = new LatLng(args.getDouble("focus_lat"), args.getDouble("focus_lng"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(focus, 16f));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
        }

        viewModel.reports.observe(getViewLifecycleOwner(), reports -> {
            allReports = reports;
            applyFilter();
        });
    }

    private void applyFilter() {
        if (googleMap == null) return;
        List<FaultReport> filtered = new ArrayList<>();
        for (FaultReport r : allReports) {
            if (activeFilter.equals("all") || r.getStatus().equals(activeFilter)) {
                filtered.add(r);
            }
        }
        updateMarkers(filtered);
    }

    private void updateMarkers(List<FaultReport> reports) {
        googleMap.clear();
        for (FaultReport report : reports) {
            LatLng position = new LatLng(report.getLatitude(), report.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(report.getTitle())
                    .snippet(report.getCategory() + " — " + report.getStatus())
                    .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(report.getStatus()))));
        }
    }

    private float getMarkerColor(String status) {
        switch (status) {
            case "in_progress": return BitmapDescriptorFactory.HUE_ORANGE;
            case "resolved":    return BitmapDescriptorFactory.HUE_GREEN;
            default:            return BitmapDescriptorFactory.HUE_RED;
        }
    }

    private void openSubmitReport() {
        SubmitReportFragment fragment = new SubmitReportFragment();
        if (googleMap != null) {
            android.os.Bundle args = new android.os.Bundle();
            com.google.android.gms.maps.model.LatLng centre = googleMap.getCameraPosition().target;
            args.putDouble("lat", centre.latitude);
            args.putDouble("lng", centre.longitude);
            fragment.setArguments(args);
        }
        fragment.show(getChildFragmentManager(), "submit_report");
    }
}
