package com.rzm.news;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.rzm.arouter_annotations.ARouter;
import com.rzm.arouter_annotations.Parameter;
import com.rzm.arouter_api.ParameterManager;

@ARouter(path = "/news/MainActivity")
public class MainActivity extends AppCompatActivity {
    @Parameter
    int age;

    @Parameter
    String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParameterManager.getInstance().loadParameter(this);
    }
}