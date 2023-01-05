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
import java.util.Map;

import io.kindling.agent.api.SystemPropertyKey;

public class ServiceFactory {
    private static volatile ServiceManager SERVICE_MANAGER = ServiceManager.NO_OP;
    public static volatile ILogService LOG = ILogService.NO_OP;
    public static volatile ICacheService CACHE = ICacheService.NO_OP;
    public static volatile IDetachAgentService DETACH_AGENT = IDetachAgentService.NO_OP;

    public static boolean isAgentActive() {
        return ServiceManager.NO_OP.equals(SERVICE_MANAGER) == false;
    }

    public static void setServiceManager(ServiceManager serviceManager) {
        if (serviceManager != null) {
            SERVICE_MANAGER = serviceManager;
            SERVICE_MANAGER.start();
            SystemPropertyKey.markKindlingAttached();
        }
    }

    public static void setLogService(ILogService service) {
        if (service != null) {
            LOG = service;
        }
    }

    public static void setDetachAgentService(IDetachAgentService service) {
        if (service != null) {
            DETACH_AGENT = service;
        }
    }

    public static void registryAgent(String className, String methodName) {
        DETACH_AGENT.registryAgent(className, methodName);
    }

    public static void setCacheService(ICacheService service) {
        if (service != null) {
            CACHE = service;
        }
    }

    public static void stopService(Map<String, String> featureMap, Instrumentation inst) {
        DETACH_AGENT.detachAgents(featureMap, inst);
        DETACH_AGENT = IDetachAgentService.NO_OP;

        SERVICE_MANAGER.stop();
        SERVICE_MANAGER = ServiceManager.NO_OP;
        CACHE.cleanCache();
        CACHE = ICacheService.NO_OP;

        SystemPropertyKey.markKindlingDetached();
    }
}
