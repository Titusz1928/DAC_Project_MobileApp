package com.example.dac_project.models;

import com.google.android.gms.maps.model.LatLng;

public class SearchResult {
    private boolean success;
    private LatLng coordinates;
    private String address;
    private String errorMessage;

    public SearchResult(boolean success, LatLng coordinates, String address, String errorMessage) {
        this.success = success;
        this.coordinates = coordinates;
        this.address = address;
        this.errorMessage = errorMessage;
    }

    // Getters for the fields
    public boolean isSuccess() {
        return success;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public String getAddress() {
        return address;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
