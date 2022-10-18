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

public class LoaderTree<T> {
    private Map<String, Map<ClassLoader, T>> loaderMap = new ConcurrentHashMap<String, Map<ClassLoader, T>>();
    
    public T getValue(ClassLoader loader, String name) {
        Map<ClassLoader, T> valueMap = loaderMap.get(name);
        if (valueMap != null) {
            if (valueMap.containsKey(loader)) {
                return valueMap.get(loader);
            }

            ClassLoader parentLoader = (loader == null) ? null : loader.getParent();
            while (parentLoader != null && !parentLoader.getClass().equals(ClassLoader.class)) {
                if (valueMap.containsKey(parentLoader)) {
                    return valueMap.get(parentLoader);
                }
                if(parentLoader.equals(loader.getParent())) {
                    //避免死循环
                    break;
                }
                parentLoader = loader.getParent();
            }
        }
        return null;
    }
    
    public void setValue(ClassLoader loader, String name, T value) {
        if (!loaderMap.containsKey(name)) {
            loaderMap.put(name, new HashMap<ClassLoader, T>());
        }
        loaderMap.get(name).put(loader, value);
    }
    
    public void clear() {
        loaderMap.clear();
    }
}