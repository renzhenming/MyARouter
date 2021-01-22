package com.rzm.arouter_api;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BundleManager {

    private Bundle bundle = new Bundle();

    public Bundle getBundle() {
        return this.bundle;
    }

    public BundleManager withString(@NonNull String key, @Nullable String value) {
        bundle.putString(key, value);
        return this; // 链式调用效果 模仿开源框架
    }

    public BundleManager withBoolean(@NonNull String key, @Nullable boolean value) {
        bundle.putBoolean(key, value);
        return this;
    }

    public BundleManager withInt(@NonNull String key, @Nullable int value) {
        bundle.putInt(key, value);
        return this;
    }

    public BundleManager withBundle(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    public Object navigation(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return RouterManager.getInstance().navigation(context, this);
        }
        return null;
    }
}
