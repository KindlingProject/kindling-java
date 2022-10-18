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

import java.util.Collection;

import io.kindling.agent.instrument.aspect.advice.info.AdviceContext;
import io.kindling.agent.instrument.aspect.advice.info.AnnotatedAdviceInfo;
import io.kindling.agent.instrument.aspect.pointcut.AspectRegistry;
import io.kindling.agent.service.AbstractService;
import io.kindling.agent.service.ServiceFactory;

public class AopRegistryService extends AbstractService {
    public AopRegistryService() {
        super("Aop Registry", true, false);
    }

    public void doStart() {
        int annotatedAdvices = 0;
        Collection<AnnotatedAdviceInfo> annotatedAdviceInfos = AdviceContext.getAdviceList();
        for (AnnotatedAdviceInfo annotatedAdviceInfo : annotatedAdviceInfos) {
            AspectRegistry.registAnnoationAspect(annotatedAdviceInfo.getAdviceName(), annotatedAdviceInfo.getPointCut());
            annotatedAdvices++;
        }
        ServiceFactory.LOG.info("[Regist Aspect] Annotation: {}", annotatedAdvices);
    }

    public void doStop() {
    }
}
