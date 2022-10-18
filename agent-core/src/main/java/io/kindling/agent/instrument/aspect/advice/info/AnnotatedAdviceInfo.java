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

package io.kindling.agent.instrument.aspect.advice.info;

import io.kindling.agent.instrument.aspect.pointcut.AnnotationPointCut;

public class AnnotatedAdviceInfo {
    private final String adviceName;
    private AnnotationPointCut pointCut;
    
    public AnnotatedAdviceInfo(String adviceName) {
        this.adviceName = adviceName;
    }
    
    public String getAdviceName() {
        return adviceName;
    }
    
    public AnnotationPointCut getPointCut() {
        return pointCut;
    }

    public void setPointCut(AnnotationPointCut pointCut) {
        this.pointCut = pointCut;
    }
}