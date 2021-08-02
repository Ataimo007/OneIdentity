package com.example.oneidentity.app.views;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.util.TimeUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.work.OneTimeWorkRequest;

import com.example.oneidentity.R;
import com.example.oneidentity.app.models.User;
import com.example.oneidentity.databinding.UserDetailsBinding;

import java.util.concurrent.TimeUnit;

public class SplashScreen extends Fragment {

    public SplashScreen() {
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
        View view = inflater.inflate(R.layout.splash_screen, container, false);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.postDelayed(() -> {
            if ( getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            {
//              NavDirections action = SplashScreenDirections.actionSplashScreenToUserList();  Undo spoiled the code
                NavController navController = Navigation.findNavController(view);
                navController.popBackStack();
                navController.navigate(R.id.userList);
            }
        }, TimeUnit.SECONDS.toMillis(3));
        return view;
    }
}