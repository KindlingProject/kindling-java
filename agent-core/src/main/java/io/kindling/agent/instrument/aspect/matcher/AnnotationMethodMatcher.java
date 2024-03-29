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

package io.kindling.agent.instrument.aspect.matcher;

import io.kindling.agent.api.MethodModifier;
import io.kindling.agent.instrument.aspect.matcher.method.MethodNameFactory;
import io.kindling.agent.instrument.aspect.matcher.method.MethodNameMatcher;
import io.kindling.agent.instrument.aspect.matcher.method.MethodParamsFactory;
import io.kindling.agent.instrument.aspect.matcher.method.MethodParamsMatcher;

public class AnnotationMethodMatcher {
    private final MethodModifier matchModifier;
    private final MethodNameMatcher methodNameMatcher;
    private final MethodParamsMatcher methodParamsMatcher;

    public AnnotationMethodMatcher(MethodModifier matchModifier, String matchMethod, String matchParam) {
        this.matchModifier = matchModifier;
        this.methodNameMatcher = MethodNameFactory.createMethodNameMatcher(matchMethod);
        this.methodParamsMatcher = MethodParamsFactory.createMethodParamsMatcher(matchParam);
    }

    public boolean matchMethod(int methodAccess, String methodName, String[] methodParams) {
        return methodNameMatcher.isMatch(methodName) && methodParamsMatcher.isMatch(methodParams) && matchModifier.isMatch(methodAccess);
    }
}
