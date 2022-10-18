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
import java.lang.instrument.Instrumentation;
import java.util.List;

import io.kindling.agent.instrument.aspect.RetransformService;
import io.kindling.agent.instrument.aspect.advice.info.AdviceContext;
import io.kindling.agent.instrument.aspect.matcher.structure.ClassStructureImplByAsm;
import io.kindling.agent.instrument.aspect.pointcut.AspectRegistry;
import io.kindling.agent.instrument.aspect.pointcut.DefaultAspectRegistryAdaptor;
import io.kindling.agent.instrument.classloader.ExtendLoader;
import io.kindling.agent.service.AbstractService;
import io.kindling.agent.service.ServiceFactory;

public class ClassTransfromService extends AbstractService {
    private final ClassFileTransformer classFileTransformer = new AopClassTransformer();
    private final Instrumentation inst;
    private final boolean attach;

    public ClassTransfromService(Instrumentation inst, boolean attach) {
        super("Class File Transform");
        this.inst = inst;
        this.attach = attach;
        AspectRegistry.bind(new DefaultAspectRegistryAdaptor());
    }

    protected void doStart() {
        if (this.attach) {
            if (inst.isRetransformClassesSupported()) {
                List<Class<?>> waitingReTransformClasses = RetransformService.findForRetransform(inst);

                inst.addTransformer(classFileTransformer, true);
                RetransformService.reTransformClasses(inst, waitingReTransformClasses);
            } else {
                ServiceFactory.LOG.info("[x Support Retransform]");
            }
        } else {
            inst.addTransformer(classFileTransformer, true);
            try {
                RetransformService.forceRedefinition(inst);
            } catch (Exception cause) {
                ServiceFactory.LOG.error("[x ForceRedefine Class]", cause);
            }
        }
    }

    protected void doStop() {
        if ((this.attach) && (inst.isRetransformClassesSupported())) {
            List<Class<?>> waitingReTransformClasses = RetransformService.findForRetransform(this.inst);
            inst.removeTransformer(this.classFileTransformer);
            RetransformService.reTransformClasses(this.inst, waitingReTransformClasses);
        }
        
        ClassStructureImplByAsm.clear();
        AdviceContext.clear();
        AspectRegistry.unbind();
        ExtendLoader.clear();
    }
}
