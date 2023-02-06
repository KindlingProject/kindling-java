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
package io.kindling.plugin.traceid.sw;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.EntrySpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;

import io.kindling.agent.api.AdviceConfig;
import io.kindling.agent.api.AfterAdvice;
import io.kindling.agent.api.JoinPoint;
import io.kindling.agent.api.KindlingApi;
import io.kindling.agent.api.MethodModifier;
import io.kindling.agent.instrument.annotation.AdvicePointCut;

@AdvicePointCut(
    matchModifiers = MethodModifier.NONE_BRIDGE,
    matchClasses = "org.apache.skywalking.apm.agent.core.context.trace.EntrySpan",
    matchMethods = "setLayer",
    matchParams = "(org.apache.skywalking.apm.agent.core.context.trace.SpanLayer)"
)
public class SetEndPointAdvice implements AfterAdvice {
    private final AdviceConfig ADVICE_CONFIG;

    public SetEndPointAdvice() {
        ADVICE_CONFIG = new AdviceConfig().enableThisParam().enableArg0Param();
    }

    public void after(JoinPoint joinPoint) {
       SpanLayer spanLayer = (SpanLayer) joinPoint.getArg0();
        if (SpanLayer.HTTP.equals(spanLayer)) {
            String endpoint = ((EntrySpan) joinPoint.getThis()).getOperationName();
            KindlingApi.startHttp(ContextManager.getGlobalTraceId(), endpoint, "skywalking");
        } else if (SpanLayer.RPC_FRAMEWORK.equals(spanLayer)) {
            String endpoint = ((EntrySpan) joinPoint.getThis()).getOperationName();
            KindlingApi.startRpc(ContextManager.getGlobalTraceId(), endpoint, "skywalking");
        }
    }

    public AdviceConfig getAdviceConfig() {
        return ADVICE_CONFIG;
    }
}
