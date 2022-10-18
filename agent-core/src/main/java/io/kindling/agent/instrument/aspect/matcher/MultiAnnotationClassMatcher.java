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
import io.kindling.agent.api.MethodSignature;

public class MultiAnnotationClassMatcher implements AnnotationClassMatcher {
    private List<SingleAnnotationClassMatcher> annotationClassMatchers;

    public MultiAnnotationClassMatcher(SingleAnnotationClassMatcher one, SingleAnnotationClassMatcher two) {
        this.annotationClassMatchers = new ArrayList<SingleAnnotationClassMatcher>();
        this.annotationClassMatchers.add(one);
        this.annotationClassMatchers.add(two);
    }

    public AnnotationClassMatcher addAnnotationClassMatcher(String matchClass, String matchMethod, String matchParam) {
        for (SingleAnnotationClassMatcher annotationClassMatcher : annotationClassMatchers) {
            if (annotationClassMatcher.addAnnotationMethodMatcher(matchClass, matchMethod, matchParam)) {
                return this;
            }
        }
        annotationClassMatchers.add(new SingleAnnotationClassMatcher(matchClass, matchMethod, matchParam));
        return this;
    }

    public boolean matchClass(ClassStructure classStructure) {
        for (SingleAnnotationClassMatcher annotationClassMatcher : annotationClassMatchers) {
            if (annotationClassMatcher.matchClass(classStructure)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchMethod(ClassStructure classStructure, MethodSignature methodSignature) {
        for (SingleAnnotationClassMatcher annotationClassMatcher : annotationClassMatchers) {
            if (annotationClassMatcher.matchMethod(classStructure, methodSignature)) {
                return true;
            }
        }
        return false;
    }
}
