package com.rzm.arouter_compiler;

import com.google.auto.service.AutoService;
import com.rzm.arouter_annotations.Parameter;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

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
import javax.lang.model.element.TypeElement;
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

            if (cacheMap == null || cacheMap.isEmpty()){
                return true;
            }

            TypeElement activityTypeElement = elementUtils.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
            TypeElement parameterGetTypeElement = elementUtils.getTypeElement(ProcessorConfig.AROUTER_AIP_PARAMETER_GET);


            ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT,ProcessorConfig.PARAMETER_NAME).build();

            MethodSpec methodSpec = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME).build();
            return false;

        }


        return true;
    }
}
