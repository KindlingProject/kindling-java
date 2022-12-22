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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.profiler.metadata.DefaultApiMetaDataService;

import io.kindling.agent.api.AdviceConfig;
import io.kindling.agent.api.AfterAdvice;
import io.kindling.agent.api.JoinPoint;
import io.kindling.agent.instrument.annotation.AdvicePointCut;

/**
 * DefaultEngineComponent.cachApi(methodDescriptor)
 * DefaultTraceContext.cachApi(methodDescriptor)
 *   => DefaultApiMetaDataService.cacheApi(methodDescriptor)
 */
@AdvicePointCut(
    matchClasses = "com.navercorp.pinpoint.profiler.metadata.DefaultApiMetaDataService",
    matchMethods = "cacheApi",
    matchParams = "(com.navercorp.pinpoint.bootstrap.context.MethodDescriptor)"
)
public class CacheApiAdvice implements AfterAdvice {
    private final AdviceConfig ADVICE_CONFIG;

    public CacheApiAdvice() {
        ADVICE_CONFIG = new AdviceConfig().enableThisParam().enableArg0Param().enableReturnObjectParam();
    }

    public void after(JoinPoint joinPoint) {
        Integer apiId = (Integer) joinPoint.getReturnObject();
        if (apiId == null) {
            return;
        }

        if (PpApiCache.checkAndCache()) {
            PpApiCache.cache((DefaultApiMetaDataService) joinPoint.getThis());
        } else {
            PpApiCache.cache(apiId, (MethodDescriptor) joinPoint.getArg0());
        }
    }

    public AdviceConfig getAdviceConfig() {
        return ADVICE_CONFIG;
    }
}
