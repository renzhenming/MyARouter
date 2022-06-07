package com.rzm.home;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.rzm.arouter_annotations.ARouter;

@ARouter(path = "/home/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_main);
    }
}