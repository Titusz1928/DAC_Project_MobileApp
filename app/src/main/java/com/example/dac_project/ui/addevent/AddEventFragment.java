package com.example.dac_project.ui.addevent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.widget.EditText;

import com.example.dac_project.R;
import com.example.dac_project.databinding.FragmentAddeventBinding;
import com.example.dac_project.databinding.FragmentMapBinding;
import com.example.dac_project.models.Coordinate;
import com.example.dac_project.ui.map.MapViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;

import com.google.gson.Gson;

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


    private void sendEventData(String latitude, String longitude, String title, String description) {
        // Convert latitude and longitude to double
        double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);

        // Create an object to hold the event data
        Coordinate eventData = new Coordinate(lat, lng, title, description, 0);

        // Convert the object to JSON format using Gson
        Gson gson = new Gson();
        String jsonData = gson.toJson(eventData);

        // Simulate sending the JSON data to an API by displaying it in a Toast or log
        Toast.makeText(getContext(), "Sending data: " + jsonData, Toast.LENGTH_LONG).show();
        Log.d("SendEventData", "Sending data: " + jsonData);
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
