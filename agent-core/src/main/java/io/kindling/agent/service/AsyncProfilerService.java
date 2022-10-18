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
import java.util.Map;

import io.kindling.agent.profiler.Profiler;

public class AsyncProfilerService extends AbstractService {
    private Profiler profiler;

    public AsyncProfilerService(Map<String, String> featureMap, File agentJar) {
        super("Async Profiler", true, true);
        profiler = new Profiler(featureMap, agentJar.getParent() + "/libasyncProfiler.so");
    }

    protected void doStart() throws Exception {
        profiler.start();
    }

    protected void doStop() throws Exception {
        profiler.stop();
    }
}
