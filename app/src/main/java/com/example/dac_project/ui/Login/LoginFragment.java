package com.example.dac_project.ui.Login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import android.widget.Button;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;


import com.example.dac_project.R;


public class LoginFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Find the button and set up a click listener
        Button navigateButton = view.findViewById(R.id.LOGifcvLoginButton);
        navigateButton.setOnClickListener(v -> {
            // Use NavController to navigate to the nav_home fragment
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_home);
        });

        return view;
    }
}