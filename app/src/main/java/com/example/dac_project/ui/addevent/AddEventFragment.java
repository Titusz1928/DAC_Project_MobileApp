package com.example.dac_project.ui.addevent;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.util.Log;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.widget.EditText;

import com.example.dac_project.R;
import com.example.dac_project.databinding.FragmentAddeventBinding;
import com.example.dac_project.databinding.FragmentMapBinding;
import com.example.dac_project.managers.SearchManager;
import com.example.dac_project.models.Coordinate;
import com.example.dac_project.models.SearchResult;
import com.example.dac_project.ui.map.MapViewModel;
import com.example.dac_project.managers.SearchManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;


import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public class AddEventFragment  extends Fragment implements OnMapReadyCallback {

    private FragmentAddeventBinding binding;
    private MapView mapView;
    private GoogleMap googleMap;
    private AddEventViewModel addEventViewModel;

    private CardView cardView;

    boolean isCardShown;

    private Marker marker;

    private EditText latitudeEditText;
    private EditText longitudeEditText;

    private EditText editTitle;
    private EditText editDescription;


    //private Button saveButton;

    private SearchManager searchManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize the ViewModel
        addEventViewModel = new ViewModelProvider(this).get(AddEventViewModel.class);

        // Inflate the layout
        binding = FragmentAddeventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize MapView and bind lifecycle
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        cardView = root.findViewById(R.id.addevent_card);
        latitudeEditText = root.findViewById(R.id.nonEditableField2);
        longitudeEditText = root.findViewById(R.id.nonEditableField1);
        editTitle = root.findViewById(R.id.editableTitle);
        editDescription = root.findViewById(R.id.editableDescription);

        //saveButton = root.findViewById(R.id.btn_save);

/*        // Initialize views
        markerTitle = root.findViewById(R.id.tvMarkerTitle);
        markerDescription = root.findViewById(R.id.tvMarkerDescription);
        markerImage = root.findViewById(R.id.markerImage);
        cardView = root.findViewById(R.id.marker_card);*/



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

        // Set Save Button functionality
        binding.btnSave.setOnClickListener(v -> {
            // Get the latitude and longitude from EditText fields
            String latitude = binding.nonEditableField2.getText().toString();
            String longitude = binding.nonEditableField1.getText().toString();

            // Simulate sending the data to an API as JSON
            sendEventData(latitude, longitude, editTitle.getText().toString(),editDescription.getText().toString());
        });

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


    private void sendEventData(String latitude, String longitude, String title, String description) {
        // Parse latitude and longitude
        double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);

        // Create a Coordinate object
        Coordinate eventData = new Coordinate(lat, lng, title, description, 1);

        // Convert the object to JSON using Gson
        Gson gson = new Gson();
        String jsonData = gson.toJson(eventData);

        // Log the JSON data for debugging
        Log.d("SendEventData", "Sending data: " + jsonData);

        // Create a new thread for network operations
        new Thread(() -> {
            try {
                RestTemplate restTemplate = new RestTemplate();

                // Set the target URL
                String url = getString(R.string.cloud_api_url) + "/addmarker";

                // Set the Content-Type header to application/json
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

                // Wrap the JSON data and headers in an HttpEntity
                HttpEntity<String> request = new HttpEntity<>(jsonData, headers);

                // Send the POST request
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

                // Handle the response
                if (response.getStatusCode().is2xxSuccessful()) {
                    Log.d("SendEventData", "Data sent successfully: " + response.getBody());
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> Toast.makeText(getContext(), "Data sent successfully!", Toast.LENGTH_SHORT).show());
                } else {
                    Log.e("SendEventData", "Failed to send data. Status code: " + response.getStatusCode());
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> Toast.makeText(getContext(), "Failed to send data!", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("SendEventData", "Error sending data", e);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> Toast.makeText(getContext(), "Error sending data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
        hideMarker();
        hideCardView();
    }



    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Set the default location (e.g., Timișoara, Romania)
        LatLng cityLocation = new LatLng(45.75411, 21.22880); // Timișoara coordinates
        float zoomLevel = 10.0f; // Zoom level (higher values zoom in closer)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, zoomLevel));


        // Listen for map clicks
        //googleMap.setOnMapClickListener(latLng -> hideCardView());

        // Listen for map clicks
        googleMap.setOnMapClickListener(latLng -> {
            if (isCardShown) {
                hideCardView();
            } else {
                setCardViewHeight(0.4f);
                // Place the marker at the clicked coordinates
                if (marker != null) {
                    marker.remove(); // Remove previous marker if exists
                }
                marker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Event Location"));
                // Update the latitude and longitude in the EditText fields
                binding.nonEditableField1.setText(String.valueOf(latLng.longitude));
                binding.nonEditableField2.setText(String.valueOf(latLng.latitude));
            }
        });

    }

    private void hideCardView() {
        if (cardView != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
            params.height = 0;
            cardView.setLayoutParams(params);
            isCardShown=false;
            // Remove the marker when the card is hidden
            hideMarker();
        }
    }

    private void hideMarker() {
        if (marker != null) {
            marker.remove();
            marker = null;
        }
    }

    private void setCardViewHeight(float heightPercent) {
        if (cardView != null) {
            // Update the TextViews with data from the provided Coordinate
/*            markerTitle.setText(coordinate.name + " (" + coordinate.nr + ")");
            markerDescription.setText(coordinate.description);

            setImage(coordinate);*/


            // Update the CardView's height
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
            params.height = (int) (heightPercent * getResources().getDisplayMetrics().heightPixels); // Set height as percentage of screen height
            cardView.setLayoutParams(params); // Apply the new layout params
            isCardShown=true;
        }
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
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

}
