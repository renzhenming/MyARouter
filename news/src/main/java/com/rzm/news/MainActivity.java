package com.rzm.news;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.rzm.arouter_annotations.ARouter;
import com.rzm.arouter_annotations.Parameter;
import com.rzm.arouter_api.ParameterManager;
import com.rzm.arouter_api.RouterManager;
import com.rzm.bean.Worker;

@ARouter(path = "/news/MainActivity")
public class MainActivity extends AppCompatActivity {
    @Parameter
    int age;

    @Parameter
    String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity_main);
        ParameterManager.getInstance().loadParameter(this);
    }

    public void jumpToMine(View view) {
        RouterManager.getInstance().build("/mine/MainActivity")
                .withInt("id",111)
                .withString("name","小猫")
                .withSerializable("worker",new Worker("张三",33))
                .navigation(this);
    }
}