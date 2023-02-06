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

import java.util.ArrayList;
import java.util.List;

import io.kindling.agent.api.ClassStructure;
import io.kindling.agent.api.MethodModifier;
import io.kindling.agent.api.MethodSignature;
import io.kindling.agent.instrument.aspect.matcher.clazz.ClassMatcher;
import io.kindling.agent.instrument.aspect.matcher.clazz.ClassMatcherFactory;

public class SingleAnnotationClassMatcher implements AnnotationClassMatcher {
    private ClassMatcher classMatcher;
    private List<AnnotationMethodMatcher> annotationMethodMatchers = new ArrayList<AnnotationMethodMatcher>();

    public SingleAnnotationClassMatcher(MethodModifier matchModifier, String matchClass, String matchMethod, String matchParam) {
        this.classMatcher = ClassMatcherFactory.getInstance().createClassMatcher(matchClass);
        this.annotationMethodMatchers.add(new AnnotationMethodMatcher(matchModifier, matchMethod, matchParam));
    }

    public AnnotationClassMatcher addAnnotationClassMatcher(MethodModifier matchModifier, String matchClass, String matchMethod, String matchParam) {
        if (addAnnotationMethodMatcher(matchModifier, matchClass, matchMethod, matchParam)) {
            return this;
        }
        return new MultiAnnotationClassMatcher(this, new SingleAnnotationClassMatcher(matchModifier, matchClass, matchMethod, matchParam));
    }

    public boolean addAnnotationMethodMatcher(MethodModifier matchModifier, String matchClass, String matchMethod, String matchParam) {
        if (matchClass.equals(classMatcher.getMatchClass())) {
            this.annotationMethodMatchers.add(new AnnotationMethodMatcher(matchModifier, matchMethod, matchParam));
            return true;
        }
        return false;
    }

    public boolean matchClass(ClassStructure classStructure) {
        return classMatcher.isMatch(classStructure);
    }

    public boolean matchMethod(ClassStructure classStructure, MethodSignature methodSignature) {
        if (matchClass(classStructure) == false) {
            return false;
        }
        for (AnnotationMethodMatcher annotationMethodMatcher : annotationMethodMatchers) {
            if (annotationMethodMatcher.matchMethod(methodSignature.getAccess(), methodSignature.getMethodName(), methodSignature.getArgTypeArray())) {
                return true;
            }
        }
        return false;
    }
}
