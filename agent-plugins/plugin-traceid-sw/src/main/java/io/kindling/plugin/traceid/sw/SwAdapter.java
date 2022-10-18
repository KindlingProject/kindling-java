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

import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.TracingContext;

import io.kindling.agent.util.Reflection;

public enum SwAdapter {
    V5_7 {
        public String getSnapShotTraceId(ContextSnapshot snapShot) {
            return snapShot.getDistributedTraceId().toString();
        }

        public String getReadableTraceId(TracingContext context) {
            return context.getReadableGlobalTraceId();
        }
    },
    V8 {
        public String getSnapShotTraceId(ContextSnapshot snapShot) {
            return snapShot.getTraceId().getId();
        }

        public String getReadableTraceId(TracingContext context) {
            return context.getReadablePrimaryTraceId();
        }
    };

    public static SwAdapter getAdapter(Class<?> traceContext) {
        if (Reflection.hasMethod(traceContext, "getReadableGlobalTraceId")) {
            return V5_7;
        }
        return V8;
    }

    public abstract String getSnapShotTraceId(ContextSnapshot snapShot);
    public abstract String getReadableTraceId(TracingContext context);
}
