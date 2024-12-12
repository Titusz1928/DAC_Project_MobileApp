package com.example.dac_project.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dac_project.R;
import com.example.dac_project.databinding.FragmentMapBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.ArrayList;
import java.util.List;
import androidx.cardview.widget.CardView;
import android.widget.LinearLayout;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private MapView mapView;
    private GoogleMap googleMap;
    private MapViewModel mapViewModel;

    public class Coordinate {
        public double lat;
        public double lng;
        public String name;
        public String description;
        public int nr;

        private CardView cardView;

        Coordinate(double lat,double lng,String name,String descitption, int nr){
            this.lat=lat;
            this.lng=lng;
            this.name=name;
            this.description=descitption;
            this.nr=nr;
        }
    }


    private CardView cardView;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize the ViewModel
        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        // Inflate the layout
        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize MapView and bind lifecycle
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize the cardView after binding the layout
        cardView = root.findViewById(R.id.marker_card);  // Accessing cardView correctly from the root view

        setCardViewHeight(0.0f);

        return root;
    }

    private void setCardViewHeight(float heightPercent) {
        if (cardView != null) {
            // Update the CardView's height using LinearLayout params
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
            params.height = (int) (heightPercent * getResources().getDisplayMetrics().heightPixels); // Set height as percentage of screen height
            cardView.setLayoutParams(params); // Apply the new layout params
        }
    }

    private void setMapViewHeight(float heightPercent) {
        // Update the MapView's height using ConstraintLayout params
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mapView.getLayoutParams();
        params.dimensionRatio = "H," + heightPercent; // Set the height ratio based on the percentage
        mapView.setLayoutParams(params); // Apply the new layout params
    }
@Override
public void onMapReady(@NonNull GoogleMap map) {
    this.googleMap = map;

    // Set the default location (e.g., Timișoara, Romania)
    LatLng cityLocation = new LatLng(45.75411, 21.22880); // Timișoara coordinates
    float zoomLevel = 10.0f; // Zoom level (higher values zoom in closer)
    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, zoomLevel));

    // Simulate fetching data from an API
    // Add markers for each coordinate received from the API
    for (Coordinate coord : fetchCoordinatesFromApi()) {
        MarkerOptions marker = new MarkerOptions()
                .position(new LatLng(coord.lat, coord.lng))
                .title(coord.name);

        map.addMarker(marker);
    }

    // Listen for marker clicks
    googleMap.setOnMarkerClickListener(marker -> {
        // When a marker is clicked, set MapView height to 70%
        //setMapViewHeight(0.7f);
        setCardViewHeight(0.5f);
        return false; // Don't consume the event, let it propagate
    });

    // Listen for map clicks (i.e., when not clicking a marker)
    googleMap.setOnMapClickListener(latLng -> {
        //setMapViewHeight(1.0f);
        setCardViewHeight(0.0f); // Reset CardView height when map is clicked
    });
}


    // Simulated API response
    private List<Coordinate> fetchCoordinatesFromApi() {
        List<Coordinate> coordinates = new ArrayList<>();
        // Simulate adding some data
        coordinates.add(new Coordinate(45.755, 21.23, "Location A", "Description A", 5));
        coordinates.add(new Coordinate(45.753, 21.235, "Location B", "Description B", 10));
        coordinates.add(new Coordinate(45.752, 21.238, "Location C", "Description C", 3));
        return coordinates;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDestroy();
        }
        binding = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
}
