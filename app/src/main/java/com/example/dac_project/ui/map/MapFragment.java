package com.example.dac_project.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dac_project.R;
import com.example.dac_project.databinding.FragmentMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import android.util.DisplayMetrics;

import com.example.dac_project.models.Coordinate;


import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private MapView mapView;
    private GoogleMap googleMap;
    private MapViewModel mapViewModel;
    private List<Coordinate> coordinates;

    private CardView cardView;
    private TextView markerTitle;
    private TextView markerDescription;

    private ImageView markerImage;


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

        // Initialize views
        markerTitle = root.findViewById(R.id.tvMarkerTitle);
        markerDescription = root.findViewById(R.id.tvMarkerDescription);
        markerImage = root.findViewById(R.id.markerImage);
        cardView = root.findViewById(R.id.marker_card);

        // Hide card view initially
        hideCardView();

        return root;
    }

    private void setCardViewHeight(float heightPercent, Coordinate coordinate) {
        if (cardView != null) {
            // Update the TextViews with data from the provided Coordinate
            markerTitle.setText(coordinate.name + " (" + coordinate.nr + ")");
            markerDescription.setText(coordinate.description);

            setImage(coordinate);


            // Update the CardView's height
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
            params.height = (int) (heightPercent * getResources().getDisplayMetrics().heightPixels); // Set height as percentage of screen height
            cardView.setLayoutParams(params); // Apply the new layout params
        }
    }

    private void setImage(Coordinate coordinate) {
        // Get the screen width
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;  // Screen width in pixels
        String size = screenWidth + "x500"; // Use the full screen width and a fixed height

        String apiKey = getString(R.string.google_maps_api_key); // Fetch API key from strings.xml
        String baseUrl = "https://maps.googleapis.com/maps/api/streetview"; // Base URL for the Street View API
        String location = coordinate.lat + "," + coordinate.lng; // Combine latitude and longitude
        String heading = "0"; // Optional: You can set the heading (direction of the camera)
        String pitch = "0"; // Optional: You can set the pitch (up or down angle of the camera)
        String fov = "90"; // Optional: Field of view (angle)

        // Construct the URL for Street View
        String streetViewUrl = baseUrl + "?size=" + size +
                "&location=" + location +
                "&heading=" + heading +
                "&pitch=" + pitch +
                "&fov=" + fov +
                "&key=" + apiKey;

        // Load the Street View image into ImageView using Picasso
        Picasso.get().load(streetViewUrl).into(markerImage);
    }

    private void hideCardView() {
        if (cardView != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
            params.height = 0;
            cardView.setLayoutParams(params);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Set the default location (e.g., Timișoara, Romania)
        LatLng cityLocation = new LatLng(45.75411, 21.22880); // Timișoara coordinates
        float zoomLevel = 10.0f; // Zoom level (higher values zoom in closer)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, zoomLevel));

        // Fetch data once and store it locally
        coordinates = fetchCoordinatesFromApi();

        // Add markers for each coordinate
        for (Coordinate coord : coordinates) {
            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(coord.lat, coord.lng))
                    .title(coord.name);
            map.addMarker(marker);
        }

        // Listen for marker clicks
        googleMap.setOnMarkerClickListener(marker -> {
            for (Coordinate coord : coordinates) {
                if (marker.getTitle().equals(coord.name)) {
                    setCardViewHeight(0.4f, coord);
                    break;
                }
            }
            return false; // Don't consume the event, let it propagate
        });

        // Listen for map clicks (i.e., when not clicking a marker)
        googleMap.setOnMapClickListener(latLng -> hideCardView());

        markerTitle.setOnClickListener(v -> {
            String markerText = markerTitle.getText().toString();
            for (Coordinate coord : coordinates) {
                if (markerText.startsWith(coord.name)) {
                    String message = coord.nr + " people reported seeing this";
                    Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        });
    }

    // Simulated API response
    private List<Coordinate> fetchCoordinatesFromApi() {
        List<Coordinate> coordinates = new ArrayList<>();
        // Simulate adding some data
        coordinates.add(new Coordinate(45.759, 21.234, "Street Light Outage", "The street light on the corner of Main St. and 3rd Ave. is not working at night.", 5));
        coordinates.add(new Coordinate(45.756, 21.240, "Pothole", "A large pothole has appeared near the bus stop on Elm St., causing traffic disruption.", 7));
        coordinates.add(new Coordinate(45.754, 21.237, "Traffic Jam", "Heavy traffic congestion due to an accident near the highway entrance. Vehicles are unable to move.", 8));
        coordinates.add(new Coordinate(45.750, 21.233, "Parking Problem", "There is a shortage of parking spaces near the city center, especially during weekends.", 4));
        coordinates.add(new Coordinate(45.752, 21.236, "Graffiti", "Graffiti is visible on the public library walls, causing concerns about cleanliness and safety.", 3));
        coordinates.add(new Coordinate(45.757, 21.239, "Pollution", "Air quality near the industrial zone is poor, with citizens reporting unpleasant odors and difficulty breathing.", 6));
        coordinates.add(new Coordinate(45.758, 21.241, "Broken Bench", "The bench in the public park near the fountain is broken and needs repair.", 2));
        coordinates.add(new Coordinate(45.755, 21.242, "Speeding Issue", "Residents are reporting frequent speeding on Park Ave. despite speed bumps and traffic cameras.", 9));
        coordinates.add(new Coordinate(45.751, 21.244, "Flooding", "There was significant flooding near Riverside Park during the last heavy rainstorm. Drains need to be checked.", 7));
        coordinates.add(new Coordinate(45.759, 21.245, "Littering", "Litter is accumulating around the bus station, with trash bins overflowing.", 5));
        coordinates.add(new Coordinate(45.760, 21.243, "Construction Noise", "Constant construction noise from the new building site near 5th Ave. is disturbing local residents.", 8));
        coordinates.add(new Coordinate(45.763, 21.246, "Road Closure", "The main road to the hospital is closed for maintenance, causing long delays for emergency services.", 10));
        coordinates.add(new Coordinate(45.765, 21.249, "Broken Crosswalk Light", "The pedestrian crosswalk light on Oak St. is malfunctioning, creating unsafe conditions for pedestrians.", 6));
        coordinates.add(new Coordinate(45.764, 21.250, "Public Restroom Needs Cleaning", "The public restroom near the central park is in need of cleaning and maintenance.", 4));
        coordinates.add(new Coordinate(45.766, 21.251, "Lack of Recycling Bins", "There are no recycling bins along the downtown area, leading to recyclable waste being thrown into regular trash bins.", 3));
        coordinates.add(new Coordinate(45.768, 21.252, "Illegal Dumping", "Residents have reported seeing illegal dumping of construction materials near the old factory site.", 8));
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
