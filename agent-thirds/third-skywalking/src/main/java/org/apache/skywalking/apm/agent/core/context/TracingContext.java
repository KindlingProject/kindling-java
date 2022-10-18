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

import java.util.LinkedList;

import org.apache.skywalking.apm.agent.core.context.ids.DistributedTraceId;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;
import org.apache.skywalking.apm.agent.core.context.trace.TraceSegment;

public class TracingContext {
    private TraceSegment segment;
    private LinkedList<AbstractSpan> activeSpanStack = new LinkedList<AbstractSpan>();
    private volatile boolean running;

    public String getReadablePrimaryTraceId() {
        return getPrimaryTraceId().getId();
    }

    private DistributedTraceId getPrimaryTraceId() {
        return segment.getRelatedGlobalTrace();
    }
    
    public void continued(ContextSnapshot snapshot) {
        if (snapshot.isValid()) {
//            TraceSegmentRef segmentRef = new TraceSegmentRef(snapshot);
//            this.segment.ref(segmentRef);
//            this.activeSpan().ref(segmentRef);
//            this.segment.relatedGlobalTrace(snapshot.getTraceId());
//            this.correlationContext.continued(snapshot);
//            this.extensionContext.continued(snapshot);
//            this.extensionContext.handle(this.activeSpan());
        }
    }
    
    public boolean stopSpan(AbstractSpan span) {
        AbstractSpan lastSpan = peek();
        if (lastSpan == span) {
            if (lastSpan instanceof AbstractTracingSpan) {
                AbstractTracingSpan toFinishSpan = (AbstractTracingSpan) lastSpan;
                if (toFinishSpan.finish(segment)) {
                    pop();
                }
            } else {
                pop();
            }
        } else {
            throw new IllegalStateException("Stopping the unexpected span = " + span);
        }

        finish();

        return activeSpanStack.isEmpty();
    }
    
    private void finish() {
//        if (isRunningInAsyncMode) {
//            asyncFinishLock.lock();
//        }
        try {
            boolean isFinishedInMainThread = activeSpanStack.isEmpty() && running;
            if (isFinishedInMainThread) {
                /*
                 * Notify after tracing finished in the main thread.
                 */
//                TracingThreadListenerManager.notifyFinish(this);
            }

//            if (isFinishedInMainThread && (!isRunningInAsyncMode || asyncSpanCounter == 0)) {
//                TraceSegment finishedSegment = segment.finish(isLimitMechanismWorking());
//                TracingContext.ListenerManager.notifyFinish(finishedSegment);
                running = false;
//            }
        } finally {
//            if (isRunningInAsyncMode) {
//                asyncFinishLock.unlock();
//            }
        }
    }
    
    private AbstractSpan pop() {
        return activeSpanStack.removeLast();
    }
    
    /**
     * @return the top element of 'ActiveSpanStack' only.
     */
    private AbstractSpan peek() {
        if (activeSpanStack.isEmpty()) {
            return null;
        }
        return activeSpanStack.getLast();
    }
}
