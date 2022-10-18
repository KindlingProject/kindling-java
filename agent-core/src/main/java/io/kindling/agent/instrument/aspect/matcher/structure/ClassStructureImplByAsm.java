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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.kindling.agent.api.ClassStructure;
import io.kindling.agent.deps.org.objectweb.asm.AnnotationVisitor;
import io.kindling.agent.deps.org.objectweb.asm.ClassReader;
import io.kindling.agent.deps.org.objectweb.asm.ClassVisitor;
import io.kindling.agent.deps.org.objectweb.asm.Opcodes;
import io.kindling.agent.deps.org.objectweb.asm.Type;
import io.kindling.agent.instrument.TransformUtil;
import io.kindling.agent.instrument.aspect.matcher.structure.PrimitiveClassStructure.Primitive;
import io.kindling.agent.util.AsmBridge;
import io.kindling.agent.util.LazyGet;
import io.kindling.agent.util.LruCache;
import io.kindling.agent.util.Streams;

public class ClassStructureImplByAsm extends FamilyClassStructure {
    private static final LruCache<ClassNameAndLoader, ClassStructure> classStructureCache = new LruCache<ClassNameAndLoader, ClassStructure>(1024);
    
    private final ClassReader classReader;
    private final ClassLoader loader;
    private final LazyGet<ClassStructure> superClassStructureLazyGet = new LazyGet<ClassStructure>() {
        protected ClassStructure initialValue() {
            final String superInternalClassName = classReader.getSuperName();
            if (null == superInternalClassName || "java/lang/Object".equals(superInternalClassName)) {
                return null;
            }
            return newInstance(TransformUtil.toJavaClassName(superInternalClassName));
        }
    };
    private final LazyGet<List<ClassStructure>> interfaceClassStructuresLazyGet = new LazyGet<List<ClassStructure>>() {
        protected List<ClassStructure> initialValue() {
            return newInstances(classReader.getInterfaces());
        }
    };
    private final LazyGet<List<String>> annotationTypeClassStructuresLazyGet = new LazyGet<List<String>>() {
        protected List<String> initialValue() {
            final List<String> annotationTypes = new ArrayList<String>();
            accept(new ClassVisitor(AsmBridge.ASM_API) {
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (visible) {
                        annotationTypes.add(Type.getType(desc).getClassName());
                    }
                    return super.visitAnnotation(desc, visible);
                }

            });
            return annotationTypes;
        }
    };
    
    public static void clear() {
        classStructureCache.clear();
    }
    
    ClassStructureImplByAsm(final InputStream classInputStream, final ClassLoader loader) throws IOException {
        this(Streams.read(classInputStream, true), loader);
    }

    ClassStructureImplByAsm(final byte[] classByteArray, final ClassLoader loader) {
        this.classReader = new ClassReader(classByteArray);
        this.loader = loader;
    }
    
    private boolean isBootstrapClassLoader() {
        return null == loader;
    }
    
    private InputStream getResourceAsStream(final String resourceName) {
        return isBootstrapClassLoader()
            ? Object.class.getResourceAsStream("/" + resourceName)
            : loader.getResourceAsStream(resourceName);
    }
    
    private String internalClassNameToResourceName(final String internalClassName) {
        return internalClassName + ".class";
    }
    
    private ClassStructure newInstance(final String javaClassName) {
        if (null == javaClassName) {
            return null;
        }

        if (javaClassName.endsWith("[]")) {
            return new ArrayClassStructure(newInstance(javaClassName.substring(0, javaClassName.length() - 2)));
        }

        final Primitive primitive = PrimitiveClassStructure.mappingPrimitiveByJavaClassName(javaClassName);
        if (null != primitive) {
            return new PrimitiveClassStructure(primitive);
        }

        final ClassNameAndLoader classNameAndLoader = new ClassNameAndLoader(javaClassName, loader);
        if (classStructureCache.containsKey(classNameAndLoader)) {
            return classStructureCache.get(classNameAndLoader);
        } else {
            final InputStream is = getResourceAsStream(internalClassNameToResourceName(TransformUtil.toInternalClassName(javaClassName)));
            if (null != is) {
                try {
                    final ClassStructure classStructure = new ClassStructureImplByAsm(is, loader);
                    classStructureCache.put(classNameAndLoader, classStructure);
                    return classStructure;
                } catch (Throwable cause) {
                    // ignore
                    classStructureCache.put(classNameAndLoader, null);
                } finally {
                    Streams.closeQuietly(is);
                }
            }
        }
        return null;
    }
    
    private List<ClassStructure> newInstances(final String[] javaClassNameArray) {
        final List<ClassStructure> classStructures = new ArrayList<ClassStructure>();
        if (null == javaClassNameArray) {
            return classStructures;
        }
        for (final String javaClassName : javaClassNameArray) {
            final ClassStructure classStructure = newInstance(javaClassName);
            if (null != classStructure) {
                classStructures.add(classStructure);
            }
        }
        return classStructures;
    }

    private void accept(final ClassVisitor cv) {
        classReader.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    public boolean isInterface() {
        return (Opcodes.ACC_INTERFACE & classReader.getAccess()) != 0;
    }
    
    public String getJavaClassName() {
        return TransformUtil.toJavaClassName(classReader.getClassName());
    }

    public ClassLoader getClassLoader() {
        return loader;
    }
    
    public ClassStructure getSuperClassStructure() {
        return superClassStructureLazyGet.get();
    }
    
    public List<ClassStructure> getInterfaceClassStructures() {
        return interfaceClassStructuresLazyGet.get();
    }
    
    public List<String> getAnnotationTypes() {
        return annotationTypeClassStructuresLazyGet.get();
    }
    
    public String toString() {
        return "ClassStructureImplByAsm{" + getJavaClassName() + "}";
    }
}

class EmptyClassStructure implements ClassStructure {
    public boolean isInterface() {
        return false;
    }
    
    public String getJavaClassName() {
        return null;
    }

    public ClassLoader getClassLoader() {
        return null;
    }

    public ClassStructure getSuperClassStructure() {
        return null;
    }

    public List<ClassStructure> getInterfaceClassStructures() {
        return Collections.emptyList();
    }

    public Map<String, ClassStructure> getFamilyTypeClassStructures() {
        return Collections.emptyMap();
    }
    
    public boolean isChild(String className) {
        return false;
    }

    public List<String> getAnnotationTypes() {
        return Collections.emptyList();
    }
}

class PrimitiveClassStructure extends EmptyClassStructure {
    private final Primitive primitive;

    PrimitiveClassStructure(Primitive primitive) {
        this.primitive = primitive;
    }

    public enum Primitive {
        BOOLEAN("boolean"),
        CHAR("char"),
        BYTE("byte"),
        INT("int"),
        SHORT("short"),
        LONG("long"),
        FLOAT("float"),
        DOUBLE("double"),
        VOID("void");

        private final String type;

        Primitive(final String type) {
            this.type = type;
        }
    }

    public String getJavaClassName() {
        return primitive.type;
    }

    static Primitive mappingPrimitiveByJavaClassName(final String javaClassName) {
        for (final Primitive primitive : Primitive.values()) {
            if (primitive.type.equals(javaClassName)) {
                return primitive;
            }
        }
        return null;
    }
}

class ArrayClassStructure extends EmptyClassStructure {
    private final ClassStructure elementClassStructure;

    ArrayClassStructure(ClassStructure elementClassStructure) {
        this.elementClassStructure = elementClassStructure;
    }

    public String getJavaClassName() {
        return elementClassStructure.getJavaClassName() + "[]";
    }
}

class ClassNameAndLoader {
    String className;
    ClassLoader loader;
    
    public ClassNameAndLoader(String className, ClassLoader loader) {
        this.className = className;
        this.loader = loader;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((loader == null) ? 0 : loader.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClassNameAndLoader other = (ClassNameAndLoader) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (loader == null) {
            if (other.loader != null)
                return false;
        } else if (!loader.equals(other.loader))
            return false;
        return true;
    }
}