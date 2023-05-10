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

import io.kindling.agent.api.AdviceConfig;
import io.kindling.agent.api.AroundAdvice;
import io.kindling.agent.api.JoinPoint;
import io.kindling.agent.api.KindlingApi;
import io.kindling.agent.instrument.annotation.AdvicePointCut;

@AdvicePointCut(
    matchClasses = "org.apache.skywalking.apm.agent.core.context.ContextManager",
    matchMethods = "createLocalSpan",
    matchParams = "(java.lang.String)"
)
public class CreateLocalSpanAdvice implements AroundAdvice {
    private final AdviceConfig ADVICE_CONFIG;

    public CreateLocalSpanAdvice() {
        ADVICE_CONFIG = new AdviceConfig().enableArg0Param();
    }

    public void before(JoinPoint joinPoint) {
        String operationName = (String) joinPoint.getArg0();
        if ("/ShardingSphere/executeSQL/".equals(operationName)) {
            // => ShardingSphere-0 segmentId (Ignore this).
            // => ShardingSphere-0 traceId 
            // => ShardingSphere-0 traceId
            return;
        }
        // First Local Span With ShardingShpere
        if ("N/A".equals(ContextManager.getGlobalTraceId()) 
            && operationName.startsWith("/ShardingSphere")) {
            joinPoint.setArg1(true);
        }
    }
    
    public void after(JoinPoint joinPoint) {
        if (joinPoint.getArg1() == null) {
            return;
        }

        String operateName = (String) joinPoint.getArg0();
        KindlingApi.startDb(ContextManager.getGlobalTraceId(), operateName, "skywalking");
    }

    public AdviceConfig getAdviceConfig() {
        return ADVICE_CONFIG;
    }
}
