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

import java.util.Map;

import io.kindling.agent.deps.one.profiler.Events;
import io.kindling.agent.util.AttachOptions;

public class AsyncProfilerOptions {
    private final long intervalMs;
    private final int stackDepth;
    private final AsyncProfilerEvent event;

    public AsyncProfilerOptions(Map<String, String> featureMap, AsyncProfilerEvent event) {
        this.intervalMs = AttachOptions.getIntervalMs(featureMap);
        this.stackDepth = AttachOptions.getDepth(featureMap);
        this.event = event;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public int getStackDepth() {
        return stackDepth;
    }

    public AsyncProfilerEvent getEvent() {
        return event;
    }

    public boolean enableCollectCpu() {
        return event.enableCpu();
    }

    public String getCpuCheckCommand(String command) {
        return command + ",event=" + Events.CPU;
    }

    public String getCommand(String command, String cpuEvent) {
        StringBuilder builder = new StringBuilder();
        builder.append(command);
        if (event.enableCpu() && cpuEvent != null) {
            builder.append(",event=").append(cpuEvent).append(",interval=").append(intervalMs).append("m").append(",jstackdepth=").append(stackDepth);
        }
        if (event.enableLock()) {
            builder.append(",event=").append(Events.LOCK + ",threads");
        }
        System.out.println("[Execute Profile Command] " + builder.toString());
        return builder.toString();
    }
}
