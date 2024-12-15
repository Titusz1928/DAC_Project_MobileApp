package com.example.dac_project.managers;

import com.example.dac_project.R;
import android.content.Context;
import com.example.dac_project.tasks.GeocodingTask;
import com.example.dac_project.models.SearchResult;

public class SearchManager {
    private String apiKey;

    // Constructor that accepts Context and fetches the API key from resources
    public SearchManager(Context context) {
        this.apiKey = context.getString(R.string.google_maps_api_key); // Fetch API key from resources
    }

    public void searchCoordinates(String query, SearchCallback callback) {
        new GeocodingTask(callback, apiKey).execute(query);
    }

    public interface SearchCallback {
        void onResult(SearchResult result);
        void onError(Exception e);
    }
}