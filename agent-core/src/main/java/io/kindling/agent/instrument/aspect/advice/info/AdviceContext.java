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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AdviceContext {
    private static final Map<String, AnnotatedAdviceInfo> ANNOTATED_ADVICES = new HashMap<String, AnnotatedAdviceInfo>();

    public static void clear() {
        ANNOTATED_ADVICES.clear();
    }

    public static AnnotatedAdviceInfo getAdviceInfo(String adviceName) {
        return ANNOTATED_ADVICES.get(adviceName);
    }

    public static Collection<AnnotatedAdviceInfo> getAdviceList() {
        return ANNOTATED_ADVICES.values();
    }

    public static void addAdviceInfo(AnnotatedAdviceInfo adviceInfo) {
        ANNOTATED_ADVICES.put(adviceInfo.getAdviceName(), adviceInfo);
    }
}
