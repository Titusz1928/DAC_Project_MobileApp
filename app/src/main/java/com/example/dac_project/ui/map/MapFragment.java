package com.example.dac_project.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.squareup.picasso.Callback;
import android.util.DisplayMetrics;

import com.example.dac_project.models.Coordinate;
import com.example.dac_project.managers.SearchManager;
import com.example.dac_project.models.SearchResult;

import android.os.Handler;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;



import java.util.ArrayList;
import java.util.Arrays;
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

    private SearchManager searchManager;

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

        //search elements
        EditText editTextSearch = root.findViewById(R.id.editTextSearch);
        FloatingActionButton fabSearch = root.findViewById(R.id.fabSearch);


        // Initialize the SearchManager
        searchManager = new SearchManager(requireContext());

        // Set up the search button click listener
        fabSearch.setOnClickListener(v -> {
            String query = editTextSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            } else {
                Toast.makeText(requireContext(), "Please enter a location to search.", Toast.LENGTH_SHORT).show();
            }
        });

        // Hide card view initially
        hideCardView();

        return root;
    }

    private void performSearch(String query) {
        // Execute the search using SearchManager
        searchManager.searchCoordinates(query, new SearchManager.SearchCallback() {
            @Override
            public void onResult(SearchResult result) {
                if (result.isSuccess()) {
                    LatLng location = result.getCoordinates();
                    String address = result.getAddress();

                    // Move the camera to the search location
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14.0f));

                    // Add a marker at the search location
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(address));

                    // Show a message or update the UI
                    Toast.makeText(requireContext(), "Search successful: " + address, Toast.LENGTH_SHORT).show();

                    // Remove the marker after 3 seconds
                    if (marker != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                marker.remove();
                            }
                        }, 3000); // 3000 milliseconds = 3 seconds
                    }
                } else {
                    // Handle search failure
                    Toast.makeText(requireContext(), "Not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                // Handle error (optional)
                Toast.makeText(requireContext(), "Search error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        String location = coordinate.lat + "," + coordinate.lng; // Combine latitude and longitude

        // Street View URL
        String streetViewUrl = "https://maps.googleapis.com/maps/api/streetview?size=" + size +
                "&location=" + location + "&key=" + apiKey;

        // Attempt to load the Street View image
        Picasso.get().load(streetViewUrl).into(markerImage, new Callback() {
            @Override
            public void onSuccess() {
                // Check if the image is valid by inspecting its dimensions
                if (markerImage.getDrawable() == null || markerImage.getDrawable().getIntrinsicWidth() == 0) {
                    // Image is invalid, load static map instead
                    loadStaticMapImage(location, size, apiKey);
                }
                // If it's a valid image, the ImageView will already have the Street View image loaded.
            }

            @Override
            public void onError(Exception e) {
                // If Street View fails (or if the image is a fallback text), load a static map image as fallback
                loadStaticMapImage(location, size, apiKey);
            }
        });
    }

    private void loadStaticMapImage(String location, String size, String apiKey) {
        // Load the static map image into ImageView
        String staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?center=" + location +
                "&zoom=14&size=" + size + "&key=" + apiKey;
        Picasso.get().load(staticMapUrl).into(markerImage);
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
    private List<Coordinate> fetchCoordinates() {
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
        coordinates.add(new Coordinate(45.749, 21.264, "Fallen tree", "A small tree has fallen and its blocking the pavement.", 6));
        return coordinates;
    }

    private List<Coordinate> fetchCoordinatesFromApi() {
        List<Coordinate> coordinates = new ArrayList<>();

        // Create a thread for network operations
        Thread thread = new Thread(() -> {
            try {
                // Initialize RestTemplate
                RestTemplate restTemplate = new RestTemplate();

                // Define the target URL (replace with your server's actual URL)
                String url = getString(R.string.cloud_api_url) + "/getmarkers";

                // Make a GET request and retrieve the response as a JSON array
                ResponseEntity<Coordinate[]> response = restTemplate.getForEntity(url, Coordinate[].class);

                // Check if the response is successful
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    // Convert the JSON array to a list
                    List<Coordinate> fetchedCoordinates = Arrays.asList(response.getBody());

                    // Add the fetched coordinates to the list
                    synchronized (coordinates) {
                        coordinates.addAll(fetchedCoordinates);
                    }

                    Log.d("FetchCoordinates", "Coordinates fetched successfully: " + fetchedCoordinates.size());
                } else {
                    Log.e("FetchCoordinates", "Failed to fetch coordinates. Status code: " + response.getStatusCode());
                }
            } catch (Exception e) {
                Log.e("FetchCoordinates", "Error fetching coordinates", e) ;
                fetchCoordinates();
            }
        });

        // Start the thread and wait for it to complete
        thread.start();
        try {
            thread.join(); // Wait for the thread to finish
        } catch (InterruptedException e) {
            Log.e("FetchCoordinates", "Thread interrupted", e);
        }

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
