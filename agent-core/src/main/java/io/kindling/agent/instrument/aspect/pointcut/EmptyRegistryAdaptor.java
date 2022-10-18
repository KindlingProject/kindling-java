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

public class EmptyRegistryAdaptor implements AspectRegistryAdaptor {
    public static final EmptyRegistryAdaptor EMPTY = new EmptyRegistryAdaptor();
    private static final LoggingInterceptor LOGGING_INTERCEPTOR = new LoggingInterceptor();

    public int addInterceptor(ClassStructure classStructure, ClassLoader loader, MethodSignature methodSignature) {
        return -1;
    }

    public Interceptor getInterceptor(int interceptorId) {
        return LOGGING_INTERCEPTOR;
    }

    public void registAnnoationAspect(String adviceClassName, AnnotationPointCut pointCut) {
    }

    public boolean matchClass(ClassStructure classStructer) {
        return false;
    }
}
