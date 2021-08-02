package com.example.oneidentity.app.views;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.oneidentity.R;
import com.example.oneidentity.app.models.User;
import com.example.oneidentity.databinding.UserDetailsBinding;

public class UserDetails extends Fragment {

    public UserDetails() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        UserDetailsBinding binding = UserDetailsBinding.inflate(inflater, container, false);
        User user = UserDetailsArgs.fromBundle(getArguments()).getUser();
        binding.setUser(user);
        return binding.getRoot();
    }
}