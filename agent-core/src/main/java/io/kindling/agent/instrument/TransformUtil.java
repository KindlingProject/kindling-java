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

package io.kindling.agent.instrument;

import java.io.InputStream;

import io.kindling.agent.api.ClassStructure;
import io.kindling.agent.deps.org.objectweb.asm.ClassReader;
import io.kindling.agent.deps.org.objectweb.asm.ClassWriter;
import io.kindling.agent.instrument.aspect.matcher.structure.ClassStructureFactory;
import io.kindling.agent.util.Streams;

public class TransformUtil {
    static final String[] ignoreClassPatterns = {
            "io/kindling/"
            , "org/vlis/apm/agent/"
            , "org/vlis/apm/extension/"
            , "org/vlis/apm/a/a/"
            , "sun/reflect/"
            , "com/sun/"
            , "javax/"
            , "sun/security/"
            , "java/util/"
            , "java/io/"
            , "java/awt/"
            , "java/applet/"
            , "org/w3c/"
            , "org/omg/"
            , "sun/"
            , "java/"
            , "net/sf/cglib/"
            , "org/objectweb/asm/"
            , "aj/org/objectweb/asm/"
            , "org/aspectj/"
            , "org/apache/jsp/"
            , "org/springframework/boot/loader/jar/JarURLConnection"
        };
        static final String[] ignoreClasses = {
            "$$EnhancerByProxool$$",
            "$$EnhancerBySpringCGLIB$$",
            "$$FastClassBySpringCGLIB$$",
            "$$KeyFactoryByCGLIB$$"
        };
        
        /**
         * Is current class be ignore for parse.
         * 
         * @return boolean
         */
        public static boolean isIgnored(String name) {
            for (String ignoreClass : ignoreClasses) {
                if (name.indexOf(ignoreClass) != -1) {
                    return true;
                }
            }
            for (String ignoreClass : ignoreClassPatterns) {
                if (name.startsWith(ignoreClass)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * The original getCommonSuperClass method will load these two classes, so
         * in the condition that one class is being transforming, we must override
         * this method.
         * 
         * @param cr
         * @return
         */
        public static ClassWriter newClassWriter(final int flags, final ClassLoader loader, ClassReader cr) {
            return new ClassWriter(cr, flags) {
                protected String getCommonSuperClass(String type1, String type2) {
                    InputStream inputStreamOfType1 = null, inputStreamOfType2 = null;
                    ClassLoader targetClassLoader = loader;
                    if (targetClassLoader == null) {
                        targetClassLoader = ClassLoader.getSystemClassLoader();
                    }
                    try {
                        inputStreamOfType1 = targetClassLoader.getResourceAsStream(type1 + ".class");
                        if (null == inputStreamOfType1) {
                            return "java/lang/Object";
                        }
                        inputStreamOfType2 = targetClassLoader.getResourceAsStream(type2 + ".class");
                        if (null == inputStreamOfType2) {
                            return "java/lang/Object";
                        }
                        final ClassStructure classStructureOfType1 = ClassStructureFactory.createClassStructure(inputStreamOfType1, targetClassLoader);
                        final ClassStructure classStructureOfType2 = ClassStructureFactory.createClassStructure(inputStreamOfType2, targetClassLoader);
                        if (classStructureOfType2.isChild(classStructureOfType1.getJavaClassName())) {
                            return type1;
                        }
                        if (classStructureOfType1.isChild(classStructureOfType2.getJavaClassName())) {
                            return type2;
                        }
                        if (classStructureOfType1.isInterface()
                                || classStructureOfType2.isInterface()) {
                            return "java/lang/Object";
                        }
                        ClassStructure classStructure = classStructureOfType1;
                        do {
                            classStructure = classStructure.getSuperClassStructure();
                            if (null == classStructure) {
                                return "java/lang/Object";
                            }
                        } while (!classStructureOfType2.isChild(classStructure.getJavaClassName()));
                        return toInternalClassName(classStructure.getJavaClassName());
                    } finally {
                        Streams.closeQuietly(inputStreamOfType1);
                        Streams.closeQuietly(inputStreamOfType2);
                    }
                }
            };
        }
        
        public static ClassStructure getClassStructure(String className, ClassLoader loader) {
            InputStream inputStream = null;
            try {
                inputStream = loader.getResourceAsStream(toInternalClassName(className) + ".class");
                return ClassStructureFactory.createClassStructure(inputStream, loader);
            } finally {
                Streams.closeQuietly(inputStream);
            }
        }
        
        /**
         * java's classname to internal's classname
         *
         * @param javaClassName java's classname
         * @return internal's classname
         */
        public static String toInternalClassName(String javaClassName) {
            return javaClassName.replace('.', '/');
        }

        /**
         * internal's classname to java's classname
         * java/lang/String to java.lang.String
         *
         * @param internalClassName internal's classname
         * @return java's classname
         */
        public static String toJavaClassName(String internalClassName) {
            return internalClassName.replace('/', '.');
        }
    }

