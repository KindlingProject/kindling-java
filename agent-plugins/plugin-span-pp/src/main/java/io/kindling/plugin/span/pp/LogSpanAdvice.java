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

package io.kindling.plugin.span.pp;

import com.navercorp.pinpoint.profiler.context.DefaultTrace;
import com.navercorp.pinpoint.profiler.context.SpanEvent;

import io.kindling.agent.api.AdviceConfig;
import io.kindling.agent.api.BeforeAdvice;
import io.kindling.agent.api.JoinPoint;
import io.kindling.agent.api.KindlingApi;
import io.kindling.agent.instrument.annotation.AdvicePointCut;

@AdvicePointCut(
    matchClasses = {
        "com.navercorp.pinpoint.profiler.context.AsyncChildTrace",
        "com.navercorp.pinpoint.profiler.context.DefaultTrace"
    },
    matchMethods = "logSpan",
    matchParams = "(com.navercorp.pinpoint.profiler.context.SpanEvent)"
)
public class LogSpanAdvice implements BeforeAdvice {
    private final AdviceConfig ADVICE_CONFIG;

    public LogSpanAdvice() {
        ADVICE_CONFIG = new AdviceConfig().enableThisParam().enableArg0Param();
    }

    public void before(JoinPoint joinPoint) {
        SpanEvent spanEvent = (SpanEvent) joinPoint.getArg0();
        if (spanEvent == null) {
            return;
        }

        String apiName = PpApiCache.getApiName(spanEvent.getApiId());
        if (apiName == null) {
            return;
        }
        if (spanEvent.getStartTime() == 0L) {
            return;
        }
        DefaultTrace trace = ((DefaultTrace) joinPoint.getThis());
        KindlingApi.recordSpan(trace.getTraceId().getTransactionId(), apiName, spanEvent.getStartTime());
    }

    public AdviceConfig getAdviceConfig() {
        return ADVICE_CONFIG;
    }
}
