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

package org.apache.skywalking.apm.agent.core.context.trace;

import org.apache.skywalking.apm.agent.core.context.TracingContext;

public abstract class AbstractTracingSpan implements AbstractSpan {
    protected int spanId;
    /**
     * Parent span id starts from 0. -1 means no parent span.
     */
    protected int parentSpanId;
    protected String operationName;
    protected SpanLayer layer;

    protected final TracingContext owner;
    protected long startTime;
    protected long endTime;

    protected AbstractTracingSpan(int spanId, int parentSpanId, String operationName, TracingContext owner) {
        this.operationName = operationName;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
        this.owner = owner;
    }
    
    public AbstractTracingSpan start() {
        this.startTime = System.currentTimeMillis();
        return this;
    }

    public AbstractSpan start(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public boolean finish(TraceSegment owner) {
        this.endTime = System.currentTimeMillis();
        owner.archive(this);
        return true;
    }

    @Override
    public AbstractTracingSpan setLayer(SpanLayer layer) {
        this.layer = layer;
        return this;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }
}
