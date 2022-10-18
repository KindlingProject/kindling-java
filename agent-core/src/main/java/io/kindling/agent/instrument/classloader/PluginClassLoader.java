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

import io.kindling.agent.service.ServiceFactory;

public class PluginClassLoader extends ClassLoader {
    public PluginClassLoader(ClassLoader superClassLoader) {
        super(superClassLoader);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        ServiceFactory.LOG.info("[Load Class]: {}, parent: {}", name, this.getParent().getClass().getName());
        byte[] b = ExtendLoader.getClazzBytes(name);
        if (b == null) {
            return ClassLoader.getSystemClassLoader().loadClass(name);
        }
        return defineClass(name, b, 0, b.length);
    }
}
