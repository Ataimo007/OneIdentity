package com.example.oneidentity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import android.os.Bundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_app);
    }

    public static class ApplicationModel extends ViewModel {
        private final ExecutorService bgExecutor = Executors.newCachedThreadPool();


        public ExecutorService getBgExecutor() {
            return bgExecutor;
        }
    }


}