/*
 * Copyright 2022 The Kindling Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kindling.agent.instrument.aspect.matcher.structure;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import io.kindling.agent.api.ClassStructure;
import io.kindling.agent.util.LazyGet;

public class ClassStructureImplByJDK extends FamilyClassStructure {
    private final Class<?> clazz;
    private String javaClassName;
    private final LazyGet<List<String>> annotationTypesLazyGet = new LazyGet<List<String>>() {
        protected List<String> initialValue() {
            if (javaClassName.startsWith("org.springframework.boot.")
                || javaClassName.startsWith("org.springframework.cloud.")) {
                return new ArrayList<String>();
            }
            return getAnnotationTypes(clazz.getDeclaredAnnotations());
        }
    };
    public ClassStructureImplByJDK(final Class<?> clazz) {
        this.clazz = clazz;
    }

    public boolean isInterface() {
        return Modifier.isInterface(clazz.getModifiers());
    }
    
    public String getJavaClassName() {
        return null != javaClassName
            ? javaClassName
            : (javaClassName = getJavaClassName(clazz));
    }

    private String getJavaClassName(Class<?> clazz) {
        if (clazz.isArray()) {
            return getJavaClassName(clazz.getComponentType()) + "[]";
        }
        return clazz.getName();
    }

    public ClassLoader getClassLoader() {
        return clazz.getClassLoader();
    }

    private ClassStructure newInstance(final Class<?> clazz) {
        if (null == clazz) {
            return null;
        }
        return new ClassStructureImplByJDK(clazz);
    }

    private List<ClassStructure> newInstances(final Class<?>[] classArray) {
        final List<ClassStructure> classStructures = new ArrayList<ClassStructure>();
        if (null != classArray) {
            for (final Class<?> clazz : classArray) {
                final ClassStructure classStructure = newInstance(clazz);
                if (null != classStructure) {
                    classStructures.add(classStructure);
                }
            }
        }
        return classStructures;
    }
    
    public ClassStructure getSuperClassStructure() {
        // 过滤掉Object.class
        return Object.class.equals(clazz.getSuperclass())
            ? null
            : newInstance(clazz.getSuperclass());
    }

    public List<ClassStructure> getInterfaceClassStructures() {
        return newInstances(clazz.getInterfaces());
    }

    public List<String> getAnnotationTypes() {
        return annotationTypesLazyGet.get();
    }

    private List<String> getAnnotationTypes(final Annotation[] annotationArray) {
        List<String> annotationTypes = new ArrayList<String>();
        for (Annotation annotation : annotationArray) {
            if (annotation.getClass().isAnnotation()) {
                annotationTypes.add(getJavaClassName(annotation.getClass()));
            }
            for (final Class<?> annotationInterfaceClass : annotation.getClass().getInterfaces()) {
                if (annotationInterfaceClass.isAnnotation()) {
                    annotationTypes.add(getJavaClassName(annotationInterfaceClass));
                }
            }
        }
        return annotationTypes;
    }

    public String toString() {
        return "ClassStructureImplByJDK{" + javaClassName + "}";
    }
}
