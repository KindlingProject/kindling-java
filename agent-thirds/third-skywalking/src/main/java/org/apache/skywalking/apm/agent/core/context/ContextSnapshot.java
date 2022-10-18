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

import org.apache.skywalking.apm.agent.core.context.ids.DistributedTraceId;

public class ContextSnapshot {
    private DistributedTraceId traceId;
    private String traceSegmentId;
    private int spanId;
    private String parentEndpoint;

    public DistributedTraceId getTraceId() {
        return traceId;
    }
    public String getTraceSegmentId() {
        return traceSegmentId;
    }
    public int getSpanId() {
        return spanId;
    }
    public String getParentEndpoint() {
        return parentEndpoint;
    }
    
    public boolean isValid() {
        return traceSegmentId != null && spanId > -1 && traceId != null;
    }
}
