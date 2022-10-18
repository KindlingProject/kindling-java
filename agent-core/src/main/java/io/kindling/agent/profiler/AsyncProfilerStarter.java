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

import io.kindling.agent.deps.one.profiler.AsyncProfiler;
import io.kindling.agent.deps.one.profiler.Events;
import io.kindling.agent.service.ServiceFactory;

public enum AsyncProfilerStarter {
    NO_CPU {
        public void start(AsyncProfiler instance, AsyncProfilerOptions options) throws Exception {
            instance.execute(options.getCommand("start", null));
        }
    },
    UNKNOWN_CPU {
        public void start(AsyncProfiler instance, AsyncProfilerOptions options) throws Exception {
            try {
                ServiceFactory.LOG.info("Check CPU...");
                instance.execute(options.getCpuCheckCommand("check"));
                ServiceFactory.LOG.info("Start CPU...");
                instance.execute(options.getCommand("start", Events.CPU));
            } catch (IllegalStateException ex) {
                try {
                    ServiceFactory.LOG.info("CPU Not Supported, Cause: " + ex.getMessage() + ", Use alluser instead.");
                    instance.execute(options.getCpuCheckCommand("check,alluser"));
                    instance.execute(options.getCommand("start,alluser", Events.CPU));
                } catch (IllegalStateException alluserEx) {
                    ServiceFactory.LOG.info("CPU alluser Not Supported, Cause: " + alluserEx.getMessage() + ", Use itimer instead.");
                    instance.execute(options.getCommand("start", Events.ITIMER));
                }
            }
        }
    };

    public abstract void start(AsyncProfiler instance, AsyncProfilerOptions options) throws Exception;
}
