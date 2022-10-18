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

import io.kindling.agent.api.MethodSignature;
import io.kindling.agent.exception.AspectInitException;
import io.kindling.agent.instrument.aspect.advice.AdviceFactory;
import io.kindling.agent.instrument.aspect.advice.AdviceInfoDetail;

public class AnnotationAspect {
    private String adviceClassName;
    private AnnotationPointCut pointCut;

    public AnnotationAspect(String adviceClassName, AnnotationPointCut pointCut) {
        this.adviceClassName = adviceClassName;
        this.pointCut = pointCut;
    }

    public AnnotationPointCut getPointCut() {
        return pointCut;
    }

    public AdviceInfoDetail getAdviceInfo(ClassLoader loader, MethodSignature methodSignature) throws AspectInitException {
        try {
            return AdviceFactory.getAdviceInfo(loader, methodSignature, adviceClassName);
        } catch (Exception e) {
            throw new AspectInitException(e.getMessage(), e);
        }
    }

    public String getAdviceClassName() {
        return adviceClassName;
    }
}
