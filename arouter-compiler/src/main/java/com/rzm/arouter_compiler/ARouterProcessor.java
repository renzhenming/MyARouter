package com.rzm.arouter_compiler;

import com.google.auto.service.AutoService;
import com.rzm.arouter_annotations.ARouter;
import com.rzm.arouter_annotations.bean.RouterBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class ARouterProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Map<String, String> options;
    private Types typeUtils;
    private String moduleName;

    // 仓库一 Path  缓存一
    // Map<"personal", List<RouterBean>>
    private Map<String, List<RouterBean>> mAllPathMap = new HashMap<>(); // 目前是一个

    // 仓库二 Group 缓存二
    // Map<"personal", "ARouter$$Path$$personal.class(自动生成的类名)">
    private Map<String, String> mAllGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        options = processingEnvironment.getOptions();
        typeUtils = processingEnvironment.getTypeUtils();
        messager.printMessage(Diagnostic.Kind.NOTE, "ARouterProcessor init");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(ARouter.class.getName());
        return set;
    }

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        moduleName = options.get(ProcessorConfig.MODULE_NAME);
        messager.printMessage(Diagnostic.Kind.NOTE, "ARouterProcessor " + moduleName + " process");
        if (set.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "ARouterProcessor process set is empty");
            return false;
        }
        TypeElement activityTypeElement = elementUtils.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
        TypeMirror activityTypeMirror = activityTypeElement.asType();
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
        for (Element element : elements) {
            Name simpleName = element.getSimpleName();
            messager.printMessage(Diagnostic.Kind.NOTE, "ARouterProcessor process element = " + simpleName);

            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            String className = simpleName.toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "被@ARetuer注解的类有 packageName = " + packageName + " className = " + className);

            ARouter aRouter = element.getAnnotation(ARouter.class);
            TypeMirror elementTypeMirror = element.asType();
            if (!typeUtils.isSubtype(elementTypeMirror, activityTypeMirror)) {
                throw new RuntimeException("@ARouter注解目前仅限用于Activity类之上");
            }

            // 在循环里面，对 “路由对象” 进行封装
            RouterBean routerBean = new RouterBean.Builder()
                    .addGroup(aRouter.group())
                    .addPath(aRouter.path())
                    .addElement(element)
                    .build();
            routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY); // 最终证明是 Activity

            if (checkRouterPath(routerBean)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean Check Success:" + routerBean.toString());
                List<RouterBean> routerBeans = mAllPathMap.get(routerBean.getGroup());

                // 如果从Map中找不到key为：bean.getGroup()的数据，就新建List集合再添加进Map
                if (routerBeans == null || routerBeans.isEmpty()) { // 仓库一 没有东西
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    mAllPathMap.put(routerBean.getGroup(), routerBeans);// 加入仓库一
                } else {
                    routerBeans.add(routerBean);
                }
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            }

            //传完整类名进去，得到type
            TypeElement pathType = elementUtils.getTypeElement(ProcessorConfig.AROUTER_API_PATH); // ARouterPath描述
            TypeElement groupType = elementUtils.getTypeElement(ProcessorConfig.AROUTER_API_GROUP); // ARouterGroup描述

            createPathFile(pathType); // 生成 Path类

            createGroupFile(groupType, pathType);
        }
        return true;
    }

    private void createGroupFile(TypeElement groupType, TypeElement pathType) {

    }

    private void createPathFile(TypeElement pathType) {

    }

    /**
     * 校验@ARouter注解的值，如果group未填写就从必填项path中截取数据
     *
     * @param bean 路由详细信息，最终实体封装类
     */
    private final boolean checkRouterPath(RouterBean bean) {
        String group = bean.getGroup(); //  "app"   "order"   "personal"
        String path = bean.getPath();   //  "/app/MainActivity"   "/order/Order_MainActivity"   "/personal/Personal_MainActivity"

        // @ARouter注解中的path值，必须要以 / 开头（模仿阿里Arouter规范）
        if (path == null || path.length() == 0 || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
            return false;
        }

        // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
        if (path.lastIndexOf("/") == 0) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            return false;
        }

        if (group == null || group.length() == 0) {
            // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app,order,personal 作为group
            group = path.substring(1, path.indexOf("/", 1));
        }

        // app,order,personal == options

        // @ARouter注解中的group有赋值情况
        if (!group.equals(moduleName)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            bean.setGroup(group);
        }

        // 如果真的返回ture   RouterBean.group  xxxxx 赋值成功 没有问题
        return true;
    }
}