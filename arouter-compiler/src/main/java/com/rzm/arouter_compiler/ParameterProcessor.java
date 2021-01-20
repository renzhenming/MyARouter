package com.rzm.arouter_compiler;

import com.google.auto.service.AutoService;
import com.rzm.arouter_annotations.Parameter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class ParameterProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Map<String, String> options;
    private Messager messager;
    private Filer filer;
    private Elements elementUtils;
    private String moduleName;
    private HashMap<TypeElement, List<Element>> cacheMap = new HashMap<>();

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(Parameter.class.getName());
        return set;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        typeUtils = processingEnvironment.getTypeUtils();
        options = processingEnvironment.getOptions();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        elementUtils = processingEnvironment.getElementUtils();

        moduleName = options.get(ProcessorConfig.MODULE_NAME);
    }

    /**
     * public class Personal_MainActivity$$Parameter implements ParameterGet {
     * *   @Override
     * *   public void getParameter(Object targetParameter) {
     * *     Personal_MainActivity t = (Personal_MainActivity) targetParameter;
     * *     t.name = t.getIntent().getStringExtra("name");
     * *     t.sex = t.getIntent().getStringExtra("sex");
     * *   }
     * * }
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE, "ParameterProcessor " + moduleName + " process start");


        if (set != null && !set.isEmpty()) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);
            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "被@Parameter注解的有 = " + element.getSimpleName());
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                    messager.printMessage(Diagnostic.Kind.NOTE, "enclosingElement = " + enclosingElement);
                    List<Element> elementList = cacheMap.get(enclosingElement);
                    if (elementList == null) {
                        elementList = new ArrayList<>();
                        elementList.add(element);
                        cacheMap.put(enclosingElement, elementList);
                    } else {
                        elementList.add(element);
                    }
                }
                messager.printMessage(Diagnostic.Kind.NOTE, "cacheMap size = " + cacheMap.size());
            }

            if (cacheMap == null || cacheMap.isEmpty()) {
                return true;
            }

            TypeElement activityTypeElement = elementUtils.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
            TypeElement parameterGetTypeElement = elementUtils.getTypeElement(ProcessorConfig.AROUTER_AIP_PARAMETER_GET);


            ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, ProcessorConfig.PARAMETER_NAME).build();

            /*
             * public class Personal_MainActivity$$Parameter implements ParameterGet {
             * *   @Override
             * *   public void getParameter(Object targetParameter) {
             * *     Personal_MainActivity t = (Personal_MainActivity) targetParameter;
             * *     t.name = t.getIntent().getStringExtra("name");
             * *     t.sex = t.getIntent().getStringExtra("sex");
             * *   }
             * * }
             */

            for (Map.Entry<TypeElement, List<Element>> typeElementListEntry : cacheMap.entrySet()) {
                MethodSpec.Builder builder = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME)
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addParameter(parameterSpec);

                TypeElement classElement = typeElementListEntry.getKey();
                ClassName className = ClassName.get(classElement);

                builder.addStatement("$T t = ($T) " + ProcessorConfig.PARAMETER_NAME, className, className);

                List<Element> parameterElementList = typeElementListEntry.getValue();
                for (Element element : parameterElementList) {
                    // 遍历注解的属性节点 生成函数体
                    TypeMirror typeMirror = element.asType();

                    // 获取 TypeKind 枚举类型的序列号
                    int type = typeMirror.getKind().ordinal();

                    // 获取属性名  name  age  sex
                    String fieldName = element.getSimpleName().toString();

                    // 获取注解的值
                    String annotationValue = element.getAnnotation(Parameter.class).name();

                    // 配合： t.age = t.getIntent().getBooleanExtra("age", t.age ==  9);
                    // 判断注解的值为空的情况下的处理（注解中有name值就用注解值）
                    annotationValue = (annotationValue == null || annotationValue.length() == 0) ? fieldName : annotationValue;

                    // TODO 最终拼接的前缀：
                    String finalValue = "t." + fieldName;

                    // t.s = t.getIntent().
                    // TODO t.name = t.getIntent().getStringExtra("name");
                    String methodContent = finalValue + " = t.getIntent().";

                    // TypeKind 枚举类型不包含String
                    if (type == TypeKind.INT.ordinal()) {
                        // t.s = t.getIntent().getIntExtra("age", t.age);
                        methodContent += "getIntExtra($S, " + finalValue + ")";  // 有默认值
                    } else if (type == TypeKind.BOOLEAN.ordinal()) {
                        // t.s = t.getIntent().getBooleanExtra("isSuccess", t.age);
                        methodContent += "getBooleanExtra($S, " + finalValue + ")";  // 有默认值
                    } else  { // String 类型，没有序列号的提供 需要我们自己完成
                        // t.s = t.getIntent.getStringExtra("s");
                        // typeMirror.toString() java.lang.String
                        if (typeMirror.toString().equalsIgnoreCase(ProcessorConfig.STRING)) {
                            // String类型
                            methodContent += "getStringExtra($S)"; // 没有默认值
                        }
                    }

                    // 健壮代码
                    if (methodContent.endsWith(")")) { // 抱歉  全部的 getBooleanExtra  getIntExtra   getStringExtra
                        // 参数二 9 赋值进去了
                        // t.age = t.getIntent().getBooleanExtra("age", t.age ==  9);
                        builder.addStatement(methodContent, annotationValue);
                    } else {
                        messager.printMessage(Diagnostic.Kind.ERROR, "目前暂支持String、int、boolean传参");
                    }
                }

                // 最终生成的类文件名（类名$$Parameter） 例如：Personal_MainActivity$$Parameter
                String finalClassName = classElement.getSimpleName() + ProcessorConfig.PARAMETER_FILE_NAME;
                messager.printMessage(Diagnostic.Kind.NOTE, "APT生成获取参数类文件：" +
                        className.packageName() + "." + finalClassName);

                // 开始生成文件，例如：PersonalMainActivity$$Parameter
                try {
                    JavaFile.builder(className.packageName(), // 包名
                            TypeSpec.classBuilder(finalClassName) // 类名
                                    .addSuperinterface(ClassName.get(parameterGetTypeElement)) //  implements ParameterGet 实现ParameterLoad接口
                                    .addModifiers(Modifier.PUBLIC) // public修饰符
                                    .addMethod(builder.build()) // 方法的构建（方法参数 + 方法体）
                                    .build()) // 类构建完成
                            .build() // JavaFile构建完成
                            .writeTo(filer); // 文件生成器开始生成类文件
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return false;

        }


        return true;
    }
}
