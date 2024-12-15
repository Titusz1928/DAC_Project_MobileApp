package com.example.dac_project.ui.home;

import android.os.Bundle;
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

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.w3c.dom.Text;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    TextView currentTimeTV;

    private TextView infoCardContent;
    private TextView weatherText;
    private LinearLayout buttonsLayout;
    private View llInfoCardView;

    private View weatherCardView;

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

        infoCardContent = root.findViewById(R.id.icvDescriptionTextView);  // Content to hide/show
        buttonsLayout = root.findViewById(R.id.expansion_panel_buttons);
        weatherText = root.findViewById(R.id.weather_textview);

        // Initially, hide the content inside the card

        infoCardContent.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.GONE);
        weatherText.setVisibility(View.GONE);

        // Set an OnClickListener for the info card
        llInfoCardView.setOnClickListener(v -> toggleCardVisibility());
        weatherCardView.setOnClickListener(v->toggleWeatherCardVisibility());

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

    // Method to toggle the card visibility
    private void toggleCardVisibility() {
        if (infoCardContent.getVisibility() == View.VISIBLE) {
            collapseCard();
        } else {
            expandCard();
        }
    }

    private void toggleWeatherCardVisibility() {
        if (weatherText.getVisibility() == View.VISIBLE) {
            collapseWeaterCard();
        } else {
            expandWeatherCard();
        }
    }

    // Expand the card content
    private void expandCard() {
        infoCardContent.setVisibility(View.VISIBLE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    // Collapse the card content
    private void collapseCard() {
        infoCardContent.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.GONE);
    }

    private void expandWeatherCard() {
        weatherText.setVisibility(View.VISIBLE);
    }

    // Collapse the card content
    private void collapseWeaterCard() {
        weatherText.setVisibility(View.GONE);
    }


    private void fetchWeatherData() {
        // You need to replace this URL with a real weather API endpoint
        String weatherApiUrl = "https://api.open-meteo.com/v1/forecast?latitude=45.7537&longitude=21.2257&hourly=temperature_2m&forecast_days=1";

        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Make the API call and get the response
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(weatherApiUrl, String.class);
            String responseBody = response.getBody();

            // For simplicity, let's assume the response is just a temperature (you can modify it to parse actual JSON response)
            // Here, you can parse the responseBody (JSON) and get the weather data, e.g., temperature, weather condition

            // Set weather text
            weatherText.setText("Weather: " + responseBody);  // Display the raw response, modify based on actual response format

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