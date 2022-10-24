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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.kindling.agent.deps.one.profiler.AsyncProfiler;
import io.kindling.agent.service.ServiceFactory;
import io.kindling.agent.util.DefaultThreadFactory;

public class Profiler {
    private final AsyncProfilerOptions options;
    private final AsyncProfiler instance;
    private ScheduledExecutorService executor;
    private AsyncProfilerStarter starter;

    public Profiler(Map<String, String> featureMap, AsyncProfilerEvent event, String libPath) {
        this.options = new AsyncProfilerOptions(featureMap, event);
        this.instance = libPath == null ? null : AsyncProfiler.getInstance(libPath);
        if (this.instance == null) {
            ServiceFactory.LOG.error("Fail to Init Async Profiler");
        } else {
            if (options.enableCollectCpu()) {
                this.executor = Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory("Kindling AsyncProfiler", true));
                this.starter = AsyncProfilerStarter.UNKNOWN_CPU;
            } else {
                this.starter = AsyncProfilerStarter.NO_CPU;
            }
        }
    }

    public synchronized void start() throws Exception {
        if (instance == null) {
            return;
        }
        this.starter.start(this.instance, this.options);
        if (executor != null) {
            executor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    try {
                        dump();
                    } catch (Throwable cause) {
                        cause.printStackTrace();
                    }
                }
            }, options.getIntervalMs(), options.getIntervalMs(), TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void stop() throws Exception {
        if (instance == null) {
            return;
        }

        if (executor != null) {
            executor.shutdown();
        }
        this.instance.stop();
    }

    final synchronized void dump() throws Exception {
        this.instance.execute("print");
    }
}
