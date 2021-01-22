package com.rzm.arouter_api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.LruCache;

import com.rzm.arouter_annotations.bean.RouterBean;

import java.util.Map;

public class RouterManager {

    private static RouterManager instance;
    private LruCache<String, ARouterGroup> groupLruCache;
    private LruCache<String, ARouterPath> pathLruCache;
    private String path;
    private String group;
    private final static String CLASS_PREFIX = "ARouter$$Group$$";

    private RouterManager() {
        groupLruCache = new LruCache<>(100);
        pathLruCache = new LruCache<>(100);
    }

    public static RouterManager getInstance() {
        if (instance == null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    public BundleManager build(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new IllegalArgumentException("正确写法：如 /order/Order_MainActivity");
        }

        // TODO 可以新增验证方式

        if (path.lastIndexOf("/") == 0) { // 只写了一个 /
            throw new IllegalArgumentException("正确写法：如 /order/Order_MainActivity");
        }

        // 截取group  /order/Order_MainActivity  finalGroup=order
        String finalGroup = path.substring(1, path.indexOf("/", 1)); // finalGroup = order

        if (TextUtils.isEmpty(finalGroup)) {
            throw new IllegalArgumentException("正确写法：如 /order/Order_MainActivity");
        }

        this.path = path;
        this.group = finalGroup;

        return new BundleManager();
    }


    /**
     * public class ARouter$$Group$$mine implements ARouterGroup {
     *
     * @param context
     * @param bundleManager
     * @return
     * @Override public Map<String, Class<? extends ARouterPath>> getGroupMap() {
     * Map<String,Class<? extends ARouterPath>> groupMap = new HashMap<>();
     * groupMap.put("mine",ARouter$$Path$$mine.class);
     * return groupMap;
     * }
     * }
     * <p>
     * <p>
     * public class ARouter$$Path$$mine implements ARouterPath {
     * @Override public Map<String, RouterBean> getPathMap() {
     * Map<String,RouterBean> pathMap = new HashMap();
     * pathMap.put("/mine/MainActivity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY, MainActivity.class, "/mine/MainActivity", "mine"));
     * return pathMap;
     * }
     * }
     */
    public Object navigation(Context context, BundleManager bundleManager) {

        String finalClassName = context.getPackageName() + CLASS_PREFIX + group;
        ARouterGroup aRouterGroup = groupLruCache.get(group);
        if (aRouterGroup == null) {
            try {
                Class<?> aClass = Class.forName(finalClassName);
                ARouterGroup routerGroup = (ARouterGroup) aClass.newInstance();
                groupLruCache.put(group, routerGroup);
                aRouterGroup = routerGroup;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        if (aRouterGroup == null) {
            throw new NullPointerException("路由表group存在问题");
        }

        ARouterPath aRouterPath = pathLruCache.get(path);
        if (aRouterPath == null) {
            Map<String, Class<? extends ARouterPath>> groupMap = aRouterGroup.getGroupMap();
            Class<? extends ARouterPath> aClass = groupMap.get(group);
            if (aClass != null) {
                try {
                    ARouterPath routerPath = aClass.newInstance();
                    pathLruCache.put(path, routerPath);
                    aRouterPath = routerPath;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }

        if (aRouterPath == null) {
            throw new NullPointerException("路由表path存在问题");
        }

        Map<String, RouterBean> pathMap = aRouterPath.getPathMap();
        RouterBean routerBean = pathMap.get(path);
        if (routerBean != null) {
            RouterBean.TypeEnum typeEnum = routerBean.getTypeEnum();
            switch (typeEnum) {
                case ACTIVITY:
                    Class<?> myClass = routerBean.getMyClass();
                    Intent intent = new Intent(context, myClass);
                    intent.putExtras(bundleManager.getBundle());
                    context.startActivity(intent);
                    break;
            }

        }

        return null;
    }
}
