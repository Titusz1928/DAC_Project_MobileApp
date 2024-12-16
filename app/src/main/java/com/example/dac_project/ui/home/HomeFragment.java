package com.example.dac_project.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.dac_project.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.widget.LinearLayout;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageButton;

import com.example.dac_project.databinding.FragmentHomeBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Map;
import java.util.HashMap;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.w3c.dom.Text;

import org.json.JSONObject;
import org.json.JSONArray;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    TextView currentTimeTV;

    private TextView infoCardContent;
    private TextView weatherText;
    private LinearLayout buttonsLayout;
    private View llInfoCardView;

    private View weatherCardView;

    private View scoreCardView;

    private TextView scoreText;
    private TextView taxReductionText;

    public GraphView graph;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        currentTimeTV = root.findViewById(R.id.tcvInformationTextView);

        // Set up the timezone for Timisoara (Europe/Bucharest)
        TimeZone timisoaraTimeZone = TimeZone.getTimeZone("Europe/Bucharest");

        // Get the current time in the specific timezone
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(timisoaraTimeZone);

        // Get the current date and time in Timisoara timezone
        String currentTime = sdf.format(new Date());

        // Set the formatted time to the TextView
        currentTimeTV.setText("Current time in Timisoara:\n "+currentTime);


        // Find the views for the expandable card
        llInfoCardView = root.findViewById(R.id.llInfoCardView);
        weatherCardView = root.findViewById(R.id.weather_card_view);
        scoreCardView = root.findViewById(R.id.score_card_view);

        infoCardContent = root.findViewById(R.id.icvDescriptionTextView);  // Content to hide/show
        buttonsLayout = root.findViewById(R.id.expansion_panel_buttons);
        weatherText = root.findViewById(R.id.weather_textview);
        graph = root.findViewById(R.id.weather_graph);
        scoreText = root.findViewById(R.id.score_text);
        taxReductionText = root.findViewById(R.id.tax_reduction_text);


        // Initially, hide the content inside the card

        infoCardContent.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.GONE);
        weatherText.setVisibility(View.GONE);
        graph.setVisibility(View.GONE);
        scoreText.setVisibility(View.GONE);
        taxReductionText.setVisibility(View.GONE);

        // Set an OnClickListener for the info card
        llInfoCardView.setOnClickListener(v ->
                toggleCardVisibility(infoCardContent, buttonsLayout));
        weatherCardView.setOnClickListener(v ->
                toggleCardVisibility(weatherText, graph));
        scoreCardView.setOnClickListener(v ->
                toggleCardVisibility(scoreText, taxReductionText));

        // Set an OnClickListener for the entire fragment to close the card when clicking elsewhere
        root.setOnClickListener(v -> collapseCard());


        ImageButton browserButton = root.findViewById(R.id.llBrowserImageButton);

        // Set the OnClickListener
        browserButton.setOnClickListener(v -> {
            // Create an Intent to open a browser with the URL
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
            startActivity(browserIntent);
        });

        // Prevent clicks inside the info card from collapsing it
        llInfoCardView.setClickable(true);

        fetchWeatherData();


        return root;
    }


    private void toggleCardVisibility(View... views) {
        if (views[0].getVisibility() == View.VISIBLE) {
            collapseCard(views);
        } else {
            expandCard(views);
        }
    }


    private void expandCard(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

    // Generic function to collapse a card by making its views gone
    private void collapseCard(View... views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }


    private void fetchWeatherData() {
        // Define the API URL
        String weatherApiUrl = "https://api.open-meteo.com/v1/forecast?latitude=45.7537&longitude=21.2257&hourly=temperature_2m&forecast_days=1";

        // Run the network request on a background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String response = fetchWeatherDataFromApi(weatherApiUrl);

            // Switch back to the main thread to update UI
            getActivity().runOnUiThread(() -> {
                if (response != null) {
                    updateGraphWithWeatherData(response);
                } else {
                    weatherText.setText("Failed to fetch weather data.");
                }
            });
        });
    }

    private String fetchWeatherDataFromApi(String url) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            // Make the API call and get the response
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // Return null in case of error
        }
    }

    private void updateGraphWithWeatherData(String responseBody) {
        try {
            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONObject hourlyData = jsonResponse.getJSONObject("hourly");
            JSONArray timeArray = hourlyData.getJSONArray("time");
            JSONArray temperatureArray = hourlyData.getJSONArray("temperature_2m");

            // Prepare the data for the graph
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

            // Loop through the temperatures and add them to the graph series
            for (int i = 0; i < temperatureArray.length(); i++) {
                double timeInHours = i; // Using index as the x-axis value (time in hours)
                double temperature = temperatureArray.getDouble(i); // Get the temperature for that hour
                series.appendData(new DataPoint(timeInHours, temperature), true, temperatureArray.length());
            }

            // Add the series to the graph
            graph.addSeries(series);

            // Set the Y-Axis title (Celsius)
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getGridLabelRenderer().setVerticalAxisTitle("°C");

            // Set the X-Axis to 24 units long and label it with hours
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(24);  // Set max X to 24 to include 24:00

            // Customizing X-Axis labels to show every 4 hours (0, 4, 8, 12, 16, 20, 24)
            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);



            // Optionally set weather text for other UI updates
            weatherText.setText("Weather for today:");
        } catch (Exception e) {
            weatherText.setText("Failed to fetch weather data.");
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}