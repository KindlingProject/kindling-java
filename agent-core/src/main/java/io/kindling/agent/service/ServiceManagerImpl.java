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

package io.kindling.agent.service;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.kindling.agent.instrument.ClassTransfromService;
import io.kindling.agent.instrument.aspect.AopRegistryService;
import io.kindling.agent.instrument.aspect.ScanService;
import io.kindling.agent.profiler.AsyncProfilerEvent;
import io.kindling.agent.util.AttachOptions;

public class ServiceManagerImpl implements ServiceManager {
    private final List<Service> services;

    public ServiceManagerImpl(Map<String, String> featureMap, boolean attach, File agentJar, Instrumentation instrumentation) {
        this.services = new ArrayList<Service>();
        this.services.add(new ScanService(agentJar)); // Scan @AdvicePointCut
        this.services.add(new AopRegistryService()); // Registry Advice

        AsyncProfilerEvent event = new AsyncProfilerEvent(AttachOptions.getEvent(featureMap));
        if (event.enableTraceId()) {
            this.services.add(new ClassTransfromService(instrumentation, attach)); // ClassTransform
        }
        if (event.enableAsyncEvent()) {
            this.services.add(new AsyncProfilerService(featureMap, event, agentJar)); // AsyncProfiler
        }
    }

    public synchronized void start() {
        for (Service service : services) {
            service.start();
        }
    }

    public synchronized void stop() {
        for (Service service : services) {
            service.stop();
        }
    }
}
