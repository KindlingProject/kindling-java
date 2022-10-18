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

import io.kindling.agent.api.ClassStructure;
import io.kindling.agent.api.Interceptor;
import io.kindling.agent.api.MethodSignature;

public interface AspectRegistryAdaptor {
    int addInterceptor(ClassStructure classStructure, ClassLoader loader, MethodSignature methodSignature);

    Interceptor getInterceptor(int interceptorId);

    void registAnnoationAspect(String adviceClassName, AnnotationPointCut pointCut);

    boolean matchClass(ClassStructure classStructer);
}
