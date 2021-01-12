package com.rzm.arouter_compiler;

import com.google.auto.service.AutoService;
import com.rzm.arouter_annotations.ARouter;

import java.util.HashSet;
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
        String moduleName = options.get(ProcessorConfig.MODULE_NAME);
        messager.printMessage(Diagnostic.Kind.NOTE, "ARouterProcessor " + moduleName + " process");
        if (set.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "ARouterProcessor process set is empty");
            return false;
        }
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
        for (Element element : elements) {
            Name simpleName = element.getSimpleName();
            messager.printMessage(Diagnostic.Kind.NOTE, "ARouterProcessor process element = " + simpleName);

            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            String className = simpleName.toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "被@ARetuer注解的类有 packageName = " + packageName + " className = " + className);
        }
        return true;
    }
}