package com.rzm.arouter_api;

import android.app.Activity;
import android.util.Log;
import android.util.LruCache;

public class ParameterManager {

    private static ParameterManager instance;
    private final LruCache<String, ParameterGet> cache;
    //生成类的后缀
    static final String FILE_SUFFIX_NAME = "$$Parameter";

    private ParameterManager() {
        cache = new LruCache<>(100);
    }

    public static ParameterManager getInstance() {
        if (instance == null) {
            synchronized (ParameterManager.class) {
                if (instance == null) {
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }

    public void loadParameter(Activity activity) {
        if (activity != null) {
            String className = activity.getClass().getName();
            Log.d("","loadParameter className = " +className);
            ParameterGet parameterGet = cache.get(className);
            if (parameterGet == null) {
                try {
                    Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);
                    ParameterGet o = (ParameterGet) aClass.newInstance();
                    cache.put(className, o);
                    o.getParameter(activity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                parameterGet.getParameter(activity);
            }
        }
    }
}
