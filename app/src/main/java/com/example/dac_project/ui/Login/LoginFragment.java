package com.example.dac_project.ui.Login;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import android.widget.Button;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;


import com.example.dac_project.R;
import com.example.dac_project.MainActivity;
import com.example.dac_project.LoginActivity;
import com.example.dac_project.ui.Registration.RegistrationFragment;


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
        Button loginButton = view.findViewById(R.id.LOGifcvLoginButton);

        Button registerButton = view.findViewById(R.id.LOGifcvRegisterButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //LOGIN LOGIC HERE

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    Toast.makeText(getActivity(), "Welcome!",
                            Toast.LENGTH_SHORT).show();
                });

                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoginActivity) getActivity()).switchFragment(new RegistrationFragment());
            }
        });

        return view;
    }
}