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

package org.apache.skywalking.apm.agent.core.context;

import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;

public class ContextManager {
    private static final String EMPTY_TRACE_CONTEXT_ID = "N/A";
    private static ThreadLocal<AbstractTracerContext> CONTEXT = new ThreadLocal<AbstractTracerContext>();
    
    public static String getGlobalTraceId() {
        AbstractTracerContext context = CONTEXT.get();
        return context != null ? context.getReadablePrimaryTraceId() : EMPTY_TRACE_CONTEXT_ID;
    }

    public static AbstractSpan createEntrySpan(String operationName, ContextCarrier carrier) {
        AbstractSpan span = null;
//        AbstractTracerContext context;
//        operationName = StringUtil.cut(operationName, OPERATION_NAME_THRESHOLD);
//        if (carrier != null && carrier.isValid()) {
//            SamplingService samplingService = ServiceManager.INSTANCE.findService(SamplingService.class);
//            samplingService.forceSampled();
//            context = getOrCreate(operationName, true);
//            span = context.createEntrySpan(operationName);
//            context.extract(carrier);
//        } else {
//            context = getOrCreate(operationName, false);
//            span = context.createEntrySpan(operationName);
//        }
        return span;
    }

    private static AbstractTracerContext get() {
        return CONTEXT.get();
    }

    public static boolean isActive() {
        return get() != null;
    }
}
