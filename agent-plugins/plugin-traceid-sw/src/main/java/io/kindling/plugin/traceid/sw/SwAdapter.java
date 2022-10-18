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
