package com.rzm.home;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.rzm.arouter_annotations.ARouter;
import com.rzm.arouter_api.RouterManager;

@ARouter(path = "/home/HomeActivity")
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_home);
    }

    public void jump(View view) {
        RouterManager.getInstance().build("/mine/MainActivity")
                .withString("myId","abc")
                .withString("name","张三")
                .navigation(this);
    }
}