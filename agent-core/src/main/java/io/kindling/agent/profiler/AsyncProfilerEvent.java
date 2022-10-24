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

package io.kindling.agent.profiler;

public class AsyncProfilerEvent {
    private static final int EVENT_CPU = 1;
    private static final int EVENT_LOCK = 2;
    private static final int EVENT_TRACEID = 4;

    private final int flag;

    public AsyncProfilerEvent(String event) {
        int flag = 0;
        if (event.contains("cpu")) {
            flag |= EVENT_CPU;
        }
        if (event.contains("lock")) {
            flag |= EVENT_LOCK;
        }
        if (event.contains("traceid")) {
            flag |= EVENT_LOCK;
        }
        this.flag = flag;
    }

    public boolean enableAsyncEvent() {
        return enableCpu() || enableLock();
    }

    public boolean enableCpu() {
        return (flag & EVENT_CPU) > 0;
    }

    public boolean enableLock() {
        return (flag & EVENT_LOCK) > 0;
    }

    public boolean enableTraceId() {
        return (flag & EVENT_TRACEID) > 0;
    }
}
