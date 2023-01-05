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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import io.kindling.agent.api.ClassStructure;
import io.kindling.agent.api.SystemPropertyKey;
import io.kindling.agent.deps.org.objectweb.asm.ClassReader;
import io.kindling.agent.instrument.aspect.matcher.structure.ClassStructureFactory;
import io.kindling.agent.instrument.aspect.pointcut.AspectRegistry;
import io.kindling.agent.instrument.aspect.weave.AsmApi;
import io.kindling.agent.instrument.aspect.weave.AsmResult;
import io.kindling.agent.service.ServiceFactory;

public class AopClassTransformer implements ClassFileTransformer {
    /**
     * transform the class bytes.
     */
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if (className == null) {
                return classfileBuffer;
            }
            if (TransformUtil.isIgnored(className)) {
                return classfileBuffer;
            }
            ClassStructure classStructure = classBeingRedefined != null ? ClassStructureFactory.createClassStructure(classBeingRedefined) : ClassStructureFactory.createClassStructure(classfileBuffer, loader);

            boolean isTransformed = false;
            byte[] data = null;
            if (AspectRegistry.matchClass(classStructure)) {
                ClassReader cr = new ClassReader(classfileBuffer);
                AsmResult result = AsmApi.weave(loader, cr, classStructure);
                isTransformed = result.isModified();
                if (isTransformed) {
                    data = result.getResult();
                    SystemPropertyKey.markApmDetected();
                }
            }
            if (isTransformed == false) {
                return classfileBuffer;
            }
            return data;
        } catch (Throwable e) {
            ServiceFactory.LOG.error("[x Transform Class] " + className + ", loader : " + (loader != null ? loader : "NULL"), e);
        }
        return classfileBuffer;
    }
}
