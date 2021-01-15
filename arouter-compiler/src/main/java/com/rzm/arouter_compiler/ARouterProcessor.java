package com.rzm.arouter_compiler;

import com.google.auto.service.AutoService;
import com.rzm.arouter_annotations.ARouter;
import com.rzm.arouter_annotations.bean.RouterBean;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

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
import javax.lang.model.element.Modifier;
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
    private String aptPackage;

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

        // 只有接受到 App壳 传递过来的数据，才能证明我们的 APT环境搭建完成
        moduleName = options.get(ProcessorConfig.MODULE_NAME);
        aptPackage = options.get(ProcessorConfig.APT_PACKAGE);
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
            TypeElement pathType = elementUtils.getTypeElement("com.rzm.arouter_api.ARouterPath"); // ARouterPath描述
            TypeElement groupType = elementUtils.getTypeElement("com.rzm.arouter_api.ARouterGroup"); // ARouterGroup描述

            messager.printMessage(Diagnostic.Kind.NOTE, "pathType = " + pathType);
            messager.printMessage(Diagnostic.Kind.NOTE, "groupType = " + groupType);

            try {
                createPathFile(pathType); // 生成 Path类
            } catch (IOException e) {
                e.printStackTrace();
            }

            createGroupFile(groupType, pathType);
        }
        return true;
    }

    /**
     * public class ARouter$$Path$$app implements ARouterPath {
     * *   @Override
     * *   public Map<String, RouterBean> getPathMap() {
     * *     Map<String, RouterBean> pathMap = new HashMap<>();
     * *     pathMap.put("/app/MainActivity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY, MainActivity.class, "/app/MainActivity", "app"));
     * *     pathMap.put("/app/Main2Activity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY, Main2Activity.class, "/app/Main2Activity", "app"));
     * *     return pathMap;
     * *   }
     * * }
     *
     * @param pathType
     */
    private void createPathFile(TypeElement pathType) throws IOException {
        if (mAllPathMap == null || mAllPathMap.size() == 0) {
            return;
        }

        ParameterizedTypeName getPathMapMethodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class)
        );

        Set<Map.Entry<String, List<RouterBean>>> entries = mAllPathMap.entrySet();
        for (Map.Entry<String, List<RouterBean>> entry : entries) {
            MethodSpec.Builder getPathMapBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(getPathMapMethodReturn);

            //T 代表 class， N 代表引用 S代表字符串 L代表枚举
            getPathMapBuilder.addStatement(
                    "$T<$T,$T> $N = new $T()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    ProcessorConfig.PATH_VAR1,
                    ClassName.get(HashMap.class)
            );

            List<RouterBean> routerBeans = entry.getValue();
            for (RouterBean routerBean : routerBeans) {
                getPathMapBuilder.addStatement(
                        "$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
                        ProcessorConfig.PATH_VAR1,
                        routerBean.getPath(),
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.TypeEnum.class),
                        routerBean.getTypeEnum(),
                        ClassName.get((TypeElement) routerBean.getElement()),
                        routerBean.getPath(),
                        routerBean.getGroup()
                );
            }

            getPathMapBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);

            // 最终生成的类文件名  ARouter$$Path$$personal
            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();
            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" +
                    aptPackage + "." + finalClassName);
            TypeSpec classType = TypeSpec.classBuilder(finalClassName)
                    .addMethod(getPathMapBuilder.build())
                    .addSuperinterface(ClassName.get(pathType))
                    .addModifiers(Modifier.PUBLIC)
                    .build();

            JavaFile.builder(aptPackage, classType).build().writeTo(filer);

            mAllGroupMap.put(entry.getKey(), finalClassName);
        }
    }

    private void createGroupFile(TypeElement groupType, TypeElement pathType) {

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