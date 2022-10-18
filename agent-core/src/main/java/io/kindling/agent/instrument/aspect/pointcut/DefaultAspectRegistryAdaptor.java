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

package io.kindling.agent.instrument.aspect.pointcut;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.kindling.agent.api.ClassStructure;
import io.kindling.agent.api.Interceptor;
import io.kindling.agent.api.MethodSignature;
import io.kindling.agent.exception.AdviceInitException;
import io.kindling.agent.exception.AspectInitException;
import io.kindling.agent.instrument.aspect.advice.AdviceInfoDetail;
import io.kindling.agent.instrument.aspect.advice.MultiAdvicesInterceptor;
import io.kindling.agent.instrument.aspect.advice.SingleAdviceInterceptor;
import io.kindling.agent.service.ServiceFactory;

public final class DefaultAspectRegistryAdaptor implements AspectRegistryAdaptor {
    private static final int DEFAULT_MAX = 1024 * 128;

    private AtomicInteger interceptorId = new AtomicInteger(0);
    private Interceptor[] interceptorArray = new Interceptor[DEFAULT_MAX];
    private List<AnnotationAspect> aspects = new ArrayList<AnnotationAspect>();

    public int addInterceptor(ClassStructure classStructure, ClassLoader loader, MethodSignature methodSignature) {
        try {
            List<AdviceInfoDetail> adviceList = new ArrayList<AdviceInfoDetail>();
            for (AnnotationAspect aspect : aspects) {
                if (aspect.getPointCut().getMatcher().matchMethod(classStructure, methodSignature)) {
                    adviceList.add(aspect.getAdviceInfo(loader, methodSignature));
                }
            }
            if (adviceList.size() > 0) {
                Interceptor interceptor = null;
                if (adviceList.size() == 1) {
                    interceptor = new SingleAdviceInterceptor(methodSignature, adviceList.get(0));
                } else {
                    interceptor = new MultiAdvicesInterceptor(methodSignature, adviceList);
                }
                int newId = interceptorId.getAndIncrement();
                if (newId >= DEFAULT_MAX) {
                    ServiceFactory.LOG.error("[Ignore Advice] " + methodSignature.getSignature() + " by Array is full.");
                    return -1;
                }
                interceptorArray[newId] = interceptor;
                return newId;
            }
        } catch (AspectInitException cause) {
            ServiceFactory.LOG.error("[x Regist Advice]", cause);
        } catch (AdviceInitException cause) {
            ServiceFactory.LOG.error("[x Regist Advice]", cause);
        }
        return -1;
    }

    public Interceptor getInterceptor(int interceptorId) {
        return interceptorArray[interceptorId];
    }

    public void registAnnoationAspect(String adviceClassName, AnnotationPointCut pointCut) {
        aspects.add(new AnnotationAspect(adviceClassName, pointCut));
    }

    public boolean matchClass(ClassStructure classStructer) {
        for (AnnotationAspect aspect : aspects) {
            if (aspect.getPointCut().getMatcher().matchClass(classStructer)) {
                return true;
            }
        }
        return false;
    }
}
