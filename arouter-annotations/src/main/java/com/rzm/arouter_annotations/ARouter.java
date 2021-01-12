package com.rzm.arouter_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface ARouter {
    // 详细路由路径（必填），如："/app/MainActivity"
    String path();

    // 路由组名（选填，如果开发者不填写，可以从path中截取出来）
    String group() default "";
}