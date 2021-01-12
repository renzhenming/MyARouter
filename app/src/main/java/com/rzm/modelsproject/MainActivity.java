package com.rzm.modelsproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.rzm.arouter_annotations.ARouter;

@ARouter(path = "/app/MainActivity", group = "app")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isRelease = BuildConfig.isRelease;
        String serverUrl = BuildConfig.ServerUrl;
    }
}