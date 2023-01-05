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

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DetachAgentService implements IDetachAgentService {
    private Map<String, String> detachAgents = new HashMap<String, String>();
    
    public void registryAgent(String className, String methodName) {
        detachAgents.put(className, methodName);
    }

    public void detachAgents(Map<String, String> featureMap, Instrumentation inst) {
        for (Map.Entry<String, String> entry : detachAgents.entrySet()) {
            ServiceFactory.LOG.info("[Detach Agent] {}.{}", entry.getKey(), entry.getValue());
            try {
                Class<?> agentClass = ClassLoader.getSystemClassLoader().loadClass(entry.getKey());
                Method detachMethod = agentClass.getDeclaredMethod(entry.getValue(), Map.class, Instrumentation.class);
                detachMethod.invoke(null, featureMap, inst);
            } catch (Throwable cause) {
                ServiceFactory.LOG.error("[x Detach Agent]", cause);
            }
        }
    }
}
