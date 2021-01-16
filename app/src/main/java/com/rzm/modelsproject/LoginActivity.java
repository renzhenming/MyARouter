package com.rzm.modelsproject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.rzm.arouter_annotations.ARouter;

@ARouter(path = "/app/LoginActivity")
public class LoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }
}