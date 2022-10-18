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

import io.kindling.agent.instrument.aspect.matcher.AnnotationClassMatcher;
import io.kindling.agent.instrument.aspect.matcher.SingleAnnotationClassMatcher;

public class AnnotationPointCut {
    private AnnotationClassMatcher matcher;
    private StringBuilder expressionBuilder;

    public AnnotationPointCut(String matchClass, String matchMethod, String matchParam) {
        this.matcher = new SingleAnnotationClassMatcher(matchClass, matchMethod, matchParam);
        this.expressionBuilder = new StringBuilder(matchClass).append('.').append(matchMethod).append(matchParam);
    }

    public void addMatcher(String matchClass, String matchMethod, String matchParam) {
        this.matcher = this.matcher.addAnnotationClassMatcher(matchClass, matchMethod, matchParam);
        this.expressionBuilder.append(" || ")
            .append(matchClass).append('.').append(matchMethod).append(matchParam);
    }

    public String getExpression() {
        return expressionBuilder.toString();
    }

    public AnnotationClassMatcher getMatcher() {
        return matcher;
    }
}
