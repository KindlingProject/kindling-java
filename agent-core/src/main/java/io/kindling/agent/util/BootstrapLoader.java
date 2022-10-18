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

package io.kindling.agent.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import io.kindling.agent.service.ServiceFactory;

public abstract class BootstrapLoader {
    private static final BootstrapLoader loader = create();

    public static BootstrapLoader get() {
        return loader;
    }

    private static BootstrapLoader create() {
        try {
            return new BootstrapLoaderImpl();
        } catch (Exception cause) {
            try {
                return new Java9BootstrapLoader();
            } catch (Exception ex) {
                try {
                    return new IBMBootstrapLoader();
                } catch (Exception exc) {
                    ServiceFactory.LOG.error("[x ClassLoader IBM]", exc);
                    ServiceFactory.LOG.error("[x ClassLoader Bootstrap]", cause);
                }
            }
        }
        return new BootstrapLoader() {
            public URL getBootstrapResource(String name) {
                return null;
            }
        };
    }

    public abstract URL getBootstrapResource(String paramString);

    private static class BootstrapLoaderImpl extends BootstrapLoader {
        private final Method getBootstrapResourceMethod;

        private BootstrapLoaderImpl() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            this.getBootstrapResourceMethod = ClassLoader.class.getDeclaredMethod("getBootstrapResource", new Class[]{String.class});
            this.getBootstrapResourceMethod.setAccessible(true);
            this.getBootstrapResourceMethod.invoke(null, new Object[]{"dummy"});
        }

        public URL getBootstrapResource(String name) {
            try {
                return (URL) this.getBootstrapResourceMethod.invoke(null, new Object[]{name});
            } catch (Exception cause) {
                ServiceFactory.LOG.error(cause.toString());
            }
            return null;
        }
    }

    private static class IBMBootstrapLoader extends BootstrapLoader {
        private static final List<String> BOOTSTRAP_CLASSLOADER_FIELDS = Arrays.asList("bootstrapClassLoader", "systemClassLoader");
        private final ClassLoader bootstrapLoader;

        public IBMBootstrapLoader() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
            Field field = getBootstrapField();
            field.setAccessible(true);
            ClassLoader cl = (ClassLoader) field.get(null);
            this.bootstrapLoader = cl;
        }

        private Field getBootstrapField() throws NoSuchFieldException {
            for (String fieldName : BOOTSTRAP_CLASSLOADER_FIELDS) {
                try {
                    return ClassLoader.class.getDeclaredField(fieldName);
                } catch (Exception cause) {
                    // Do nothing.
                }
            }
            throw new NoSuchFieldException(MessageFormat.format("[x NotFound Bootstrap] fields: {0}", new Object[]{BOOTSTRAP_CLASSLOADER_FIELDS}));
        }

        public URL getBootstrapResource(String name) {
            return this.bootstrapLoader.getResource(name);
        }
    }
    
    private static class Java9BootstrapLoader extends BootstrapLoader {
        private Method methodBootLoaderFindResource;
        private ClassLoader platformClassLoader;

        private Java9BootstrapLoader() throws Exception {
            try {
                Class<?> bootLoader = Class.forName("jdk.internal.loader.BootLoader");
                this.methodBootLoaderFindResource = bootLoader.getDeclaredMethod("findResource", new Class[] { String.class });
                this.methodBootLoaderFindResource.invoke(null, new Object[] { "dummy" });
            } catch (Throwable t) {
                this.methodBootLoaderFindResource = null;
                Method getPlatformClassLoader = ClassLoader.class.getDeclaredMethod("getPlatformClassLoader", new Class[0]);
                this.platformClassLoader = ((ClassLoader) getPlatformClassLoader.invoke(null, new Object[0]));
            }
        }

        public URL getBootstrapResource(String internalOrClassName) {
            try {
                if (this.methodBootLoaderFindResource != null) {
                    return (URL) this.methodBootLoaderFindResource.invoke(null, new Object[] { getClassResourceName(internalOrClassName) });
                }
                if (this.platformClassLoader != null) {
                    return this.platformClassLoader.getResource(getClassResourceName(internalOrClassName));
                }
                return null;
            } catch (Exception e) {
            }
            return null;
        }
        
        private String getClassResourceName(String binaryName) {
            if (binaryName.endsWith(".class")) {
                return binaryName;
            }
            return binaryName.replace('.', '/') + ".class";
        }
    }
}
