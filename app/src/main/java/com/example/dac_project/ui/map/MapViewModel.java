package com.example.dac_project.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapViewModel extends ViewModel {

    // LiveData to hold the list of markers
    private final MutableLiveData<List<MarkerOptions>> markers;

    public MapViewModel() {
        // Initialize the MutableLiveData with an empty list
        markers = new MutableLiveData<>(new ArrayList<>());
    }

    // Method to get the list of markers (LiveData)
    public LiveData<List<MarkerOptions>> getMarkers() {
        return markers;
    }

    // Method to add a new marker to the list
    public void addMarker(MarkerOptions marker) {
        List<MarkerOptions> currentMarkers = markers.getValue();
        if (currentMarkers != null) {
            currentMarkers.add(marker);
            markers.setValue(currentMarkers); // Update the LiveData
        }
    }

    // Method to clear all markers
    public void clearMarkers() {
        markers.setValue(new ArrayList<>()); // Reset the markers list
    }
}
