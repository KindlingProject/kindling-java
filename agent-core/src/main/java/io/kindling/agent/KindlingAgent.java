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

package io.kindling.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Map;

import io.kindling.agent.api.AgentType;
import io.kindling.agent.profiler.AsyncProfilerOptions;
import io.kindling.agent.profiler.Profiler;
import io.kindling.agent.util.FileWriter;

public class KindlingAgent {
	private static Profiler profiler;

    public static void premain(Map<String, String> featureMap, Instrumentation instrumentation, File agentJarFile) throws Exception {
        AgentType agentType = AgentType.getAgentType(featureMap);
        if (AgentType.Detach.equals(agentType)) {
            if (profiler != null) {
            	profiler.stop();
            	profiler = null;
            }
            return;
        }

        if (profiler == null) {
            FileWriter writer = new FileWriter(AsyncProfilerOptions.getLogFile(featureMap));

            profiler = new Profiler(AsyncProfilerOptions.getIntervalMs(featureMap), AsyncProfilerOptions.getDepth(featureMap), agentJarFile.getParent() + "/libasyncProfiler.so");
            profiler.start(writer);
        }
    }
}
