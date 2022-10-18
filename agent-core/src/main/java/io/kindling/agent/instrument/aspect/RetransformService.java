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

package io.kindling.agent.instrument.aspect;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.kindling.agent.api.ClassStructure;
import io.kindling.agent.deps.org.objectweb.asm.Type;
import io.kindling.agent.instrument.TransformUtil;
import io.kindling.agent.instrument.aspect.matcher.structure.ClassStructureFactory;
import io.kindling.agent.instrument.aspect.pointcut.AspectRegistry;
import io.kindling.agent.service.ServiceFactory;
import io.kindling.agent.util.BootstrapLoader;
import io.kindling.agent.util.Streams;

public class RetransformService {
    private static List<Class<?>> getToRedefineClasses() {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(ClassLoader.class);
        return classes;
    }

    public static void forceRedefinition(Instrumentation instrumentation) throws ClassNotFoundException, UnmodifiableClassException {
        List<ClassDefinition> toRedefine = new ArrayList<ClassDefinition>();
        List<Class<?>> classes = getToRedefineClasses();
        for (Class<?> clazz : classes) {
            String classResourceName = Type.getInternalName(clazz) + ".class";
            URL resource = clazz.getResource(classResourceName);
            if (resource == null) {
                resource = BootstrapLoader.get().getBootstrapResource(classResourceName);
            }

            if (resource != null) {
                try {
                    byte[] classfileBuffer = Streams.read(resource.openStream(), true);

                    toRedefine.add(new ClassDefinition(clazz, classfileBuffer));
                } catch (Exception cause) {
                    ServiceFactory.LOG.error("[x Redefine Class] " + clazz.getName(), cause);
                }
            } else {
                ServiceFactory.LOG.error("[x Redefine Class] " + clazz.getName());
            }
        }
        if (!toRedefine.isEmpty()) {
            instrumentation.redefineClasses((ClassDefinition[]) toRedefine.toArray(new ClassDefinition[0]));
        }
    }

    public static void retransformClasses(Instrumentation instrumentation, List<String> loadedClassNames) {
        if (instrumentation.isRetransformClassesSupported()) {
            Class<?>[] loadedClasses = instrumentation.getAllLoadedClasses();

            Set<Class<?>> toReTransformClasses = new HashSet<Class<?>>();
            for (Class<?> loadedClass : loadedClasses) {
                if (loadedClassNames.contains(loadedClass.getName())) {
                    ServiceFactory.LOG.info("[Retransform class] {}", loadedClass.getName());
                    toReTransformClasses.add(loadedClass);
                }
            }
            if (toReTransformClasses.size() > 0) {
                try {
                    instrumentation.retransformClasses((Class<?>[]) toReTransformClasses.toArray(new Class[0]));
                } catch (Exception e) {
                    ServiceFactory.LOG.error("Retransform classes failed.", e);
                }
            }
        }
    }

    public static void reTransformClasses(Instrumentation inst, List<Class<?>> toReTransformClasses) {
        int total = toReTransformClasses.size();
        if (total == 0) {
            return;
        }

        ServiceFactory.LOG.info("[Size RetransClass] {}", toReTransformClasses.size());
        int successCount = 0;
        for (final Class<?> waitingReTransformClass : toReTransformClasses) {
            try {
                inst.retransformClasses(waitingReTransformClass);
                successCount++;
            } catch (Throwable cause) {
                ServiceFactory.LOG.error("[x Retransform Class] " + waitingReTransformClass.getName(), cause);
            }
        }
        ServiceFactory.LOG.info("[Result RetransClass] o: {}, x: {}.", successCount, (total - successCount));
    }

    public static List<Class<?>> findForRetransform(Instrumentation inst) {
        final List<Class<?>> classLoaders = new ArrayList<Class<?>>();
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (inst.isModifiableClass(clazz) == false || clazz.isArray() || TransformUtil.isIgnored(TransformUtil.toInternalClassName(clazz.getName()))) {
                continue;
            }

            ClassStructure classStructure = ClassStructureFactory.createClassStructure(clazz);
            try {
                if (classStructure.isChild("java.lang.ClassLoader")) {
                    classLoaders.add(clazz);
                } else if (AspectRegistry.matchClass(classStructure)) {
                    classes.add(clazz);
                }
            } catch (Throwable cause) {
                ServiceFactory.LOG.error("[x Retransform Class] " + clazz.getName(), cause);
            }
        }
        classLoaders.addAll(classes);
        return classLoaders;
    }
}
