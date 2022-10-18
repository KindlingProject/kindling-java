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

public class ServiceFactory {
    private static volatile ServiceManager SERVICE_MANAGER = ServiceManager.NO_OP;
    public static volatile ILogService LOG = ILogService.NO_OP;

    public static boolean isAgentActive() {
        return ServiceManager.NO_OP.equals(SERVICE_MANAGER) == false;
    }

    public static void setServiceManager(ServiceManager serviceManager) {
        if (serviceManager != null) {
            SERVICE_MANAGER = serviceManager;
            SERVICE_MANAGER.start();
        }
    }

    public static void setLogService(ILogService service) {
        if (service != null) {
            LOG = service;
        }
    }

    public static void stopService() {
        SERVICE_MANAGER.stop();
        SERVICE_MANAGER = ServiceManager.NO_OP;
    }
}
