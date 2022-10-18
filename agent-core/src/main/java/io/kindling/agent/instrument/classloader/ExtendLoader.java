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

package io.kindling.agent.instrument.classloader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtendLoader {
    private static final Map<ClassLoader, PluginClassLoader> CACHE_PLUGINLOADER = new ConcurrentHashMap<ClassLoader, PluginClassLoader>();
    private static final LoaderTree<Class<?>> LOAD_CLASS_TREE = new LoaderTree<Class<?>>();
    private static final Map<String, byte[]> CLASS_BYTE_MAP = new HashMap<String, byte[]>(1000);

    public static Class<?> loadPlugin(String className, ClassLoader loader) throws ClassNotFoundException {
        if (className.startsWith("io.kindling.agent.agent.") || loader == null) {
            return ClassLoader.getSystemClassLoader().loadClass(className);
        }
        if (CLASS_BYTE_MAP.containsKey(className)) {
            Class<?> cacheClass = LOAD_CLASS_TREE.getValue(loader, className);
            if (cacheClass != null) {
                return cacheClass;
            }
            cacheClass = getOrCreatePluginClassLoader(loader).loadClass(className);
            LOAD_CLASS_TREE.setValue(loader, className, cacheClass);
            return cacheClass;
        }
        return loader.loadClass(className);
    }

    private static PluginClassLoader getOrCreatePluginClassLoader(ClassLoader loader) {
        PluginClassLoader pluginClassLoader = CACHE_PLUGINLOADER.get(loader);
        if (pluginClassLoader == null) {
            CACHE_PLUGINLOADER.put(loader, pluginClassLoader = new PluginClassLoader(loader));
        }
        return pluginClassLoader;
    }

    public static byte[] getClazzBytes(String className) {
        return CLASS_BYTE_MAP.get(className);
    }

    public static void setClassByteMap(String className, byte[] bytes) {
        CLASS_BYTE_MAP.put(className, bytes);
    }

    public static void clear() {
        CACHE_PLUGINLOADER.clear();
        LOAD_CLASS_TREE.clear();
        CLASS_BYTE_MAP.clear();
    }
}
