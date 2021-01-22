package com.rzm.mine;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.rzm.arouter_annotations.ARouter;
import com.rzm.arouter_annotations.Parameter;
import com.rzm.arouter_api.RouterManager;

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

    public void jump(View view) {
            RouterManager.getInstance().build("/news/MainActivity")
                    .withInt("age",12)
                    .withString("number","32")
                    .navigation(this);
    }
}