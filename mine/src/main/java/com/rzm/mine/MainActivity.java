package com.rzm.mine;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.rzm.arouter_annotations.ARouter;
import com.rzm.arouter_annotations.Parameter;

@ARouter(path = "/mine/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Parameter(name = "myId")
    String id;

    @Parameter
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}