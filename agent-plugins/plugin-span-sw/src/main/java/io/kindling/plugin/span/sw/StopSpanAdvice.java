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

package io.kindling.plugin.span.sw;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;

import io.kindling.agent.api.AdviceConfig;
import io.kindling.agent.api.AfterAdvice;
import io.kindling.agent.api.JoinPoint;
import io.kindling.agent.api.KindlingApi;
import io.kindling.agent.instrument.annotation.AdvicePointCut;
import io.kindling.agent.util.Reflection;

import java.lang.reflect.Field;

@AdvicePointCut(
    matchClasses = "org.apache.skywalking.apm.agent.core.context.TracingContext",
    matchMethods = "stopSpan",
    matchParams = "(org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan)"
)
public class StopSpanAdvice implements AfterAdvice {
    private final AdviceConfig ADVICE_CONFIG;
    private volatile Field startTimeField;

    public StopSpanAdvice() {
        ADVICE_CONFIG = new AdviceConfig().enableArg0Param();
    }

    public void after(JoinPoint joinPoint) {
        AbstractSpan span = (AbstractSpan) joinPoint.getArg0();
        // Ignore NoopSpan
        if (span != null && "".equals(span.getOperationName()) == false) {
            try {
                if (startTimeField == null) {
                    // Cache Field
                    startTimeField = Reflection.getAccessibleField(AbstractTracingSpan.class, "startTime");
                }
                long startTime = (Long) startTimeField.get(span);
                KindlingApi.recordSpan(ContextManager.getGlobalTraceId(), span.getOperationName(), startTime);
            } catch (Throwable cause) {
            }
        }
    }

    public AdviceConfig getAdviceConfig() {
        return ADVICE_CONFIG;
    }
}
