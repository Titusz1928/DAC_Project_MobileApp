package com.example.dac_project.tasks;

import android.os.AsyncTask;
import android.util.Log;
import com.example.dac_project.models.SearchResult;
import com.example.dac_project.managers.SearchManager;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeocodingTask extends AsyncTask<String, Void, SearchResult> {
    private static final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private String apiKey;

    private SearchManager.SearchCallback callback;

    // Constructor to accept the callback
    public GeocodingTask(SearchManager.SearchCallback callback, String apiKey) {
        this.callback = callback;
        this.apiKey = apiKey;
    }

    @Override
    protected SearchResult doInBackground(String... queries) {
        String query = queries[0];
        String response = "";

        try {
            String urlString = GEOCODING_API_URL + "?address=" + query + "&key=" + apiKey;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            reader.close();
            response = builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the response wrapped in a SearchResult
        return parseGeocodingResponse(response);
    }

    @Override
    protected void onPostExecute(SearchResult result) {
        super.onPostExecute(result);
        // Pass the result to the callback
        if (callback != null) {
            callback.onResult(result);
        }
    }

    // Helper method to parse the response into a SearchResult
    private SearchResult parseGeocodingResponse(String response) {
        boolean success = false;
        LatLng coordinates = null;
        String address = "";
        String errorMessage = "";

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("OK")) {
                JSONObject location = jsonObject
                        .getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location");

                double latitude = location.getDouble("lat");
                double longitude = location.getDouble("lng");
                coordinates = new LatLng(latitude, longitude);

                address = jsonObject.getJSONArray("results")
                        .getJSONObject(0)
                        .getString("formatted_address");

                success = true;
            } else {
                errorMessage = "Geocoding failed: " + jsonObject.getString("status");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "Error parsing response.";
        }

        // Return a SearchResult with the parsed data
        return new SearchResult(success, coordinates, address, errorMessage);
    }
}
