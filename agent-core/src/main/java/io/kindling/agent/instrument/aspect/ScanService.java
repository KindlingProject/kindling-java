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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import io.kindling.agent.deps.org.objectweb.asm.ClassReader;
import io.kindling.agent.deps.org.objectweb.asm.ClassVisitor;
import io.kindling.agent.instrument.aspect.advice.AdviceScaner;
import io.kindling.agent.instrument.classloader.ExtendLoader;
import io.kindling.agent.service.AbstractService;
import io.kindling.agent.service.ServiceFactory;
import io.kindling.agent.util.Streams;

public class ScanService extends AbstractService {
    private final File agentJarFile;

    public ScanService(File agentJarFile) {
        super("Scan Jar", true, false);
        this.agentJarFile = agentJarFile;
    }

    public void doStart() {
        for (File jarFile : agentJarFile.getParentFile().listFiles()) {
            if (jarFile.getName().toLowerCase().endsWith(".jar")) {
                JarInputStream jarStream = null;
                try {
                    ServiceFactory.LOG.info("[Sacn Jar] {}", jarFile.getAbsolutePath());
                    jarStream = new JarInputStream(new FileInputStream(jarFile));
                    JarEntry entry = null;
                    String className = null;
                    while ((entry = jarStream.getNextJarEntry()) != null) {
                        if (!entry.getName().startsWith("io/kindling/agent/deps/") && entry.getName().endsWith(".class")) {
                            byte[] classByte = Streams.read(jarStream, (int) entry.getSize(), false);
                            ClassReader cr = new ClassReader(classByte);

                            ClassVisitor cv = new AdviceScaner(null);
                            cr.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                            className = entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.');
                            if (className.startsWith("io.kindling.agent.") == false) {
                                ExtendLoader.setClassByteMap(className, classByte);
                            }
                        }
                    }
                } catch (IOException exception) {
                    ServiceFactory.LOG.error("[x Sacn Jar]", exception);
                } finally {
                    if (jarStream != null) {
                        try {
                            jarStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
    }

    protected void doStop() {
    }
}
