package com.rzm.modelsproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.rzm.arouter_annotations.ARouter;
import com.rzm.arouter_annotations.bean.RouterBean;
import com.rzm.arouter_api.ARouterPath;
import com.rzm.arouter_api.RouterManager;
import com.rzm.bean.Worker;

import java.util.Map;

@ARouter(path = "/app/MainActivity", group = "app")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isRelease = BuildConfig.isRelease;
        String serverUrl = BuildConfig.ServerUrl;
    }

    private void toMineActivity() {
        ARouter$$Group$$mine mine = new ARouter$$Group$$mine();
        Map<String, Class<? extends ARouterPath>> groupMap = mine.getGroupMap();
        Class<? extends ARouterPath> mineClass = groupMap.get("mine");
        try {
            ARouter$$Path$$mine aRouterPath = (ARouter$$Path$$mine) mineClass.newInstance();
            Map<String, RouterBean> map = aRouterPath.getPathMap();
            RouterBean routerBean = map.get("/mine/MainActivity");
            Class<?> myClass = routerBean.getMyClass();
            Intent intent = new Intent(this, myClass);
            startActivity(intent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public void jumpToMine(View view) {

        Worker worker = new Worker("我是一名工人", 22);

        RouterManager.getInstance().build("/mine/MainActivity")
                .withString("myId", "123456")
                .withString("name", "章三")
                .withSerializable("worker", worker)
                .navigation(this);
    }
}