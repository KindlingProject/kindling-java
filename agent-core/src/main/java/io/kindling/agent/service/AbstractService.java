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

public abstract class AbstractService implements Service {
    private final String name;
    private final boolean startEnabled;
    private final boolean stopEnabled;

    protected AbstractService(String name) {
        this.name = name;
        this.startEnabled = true;
        this.stopEnabled = true;
    }

    protected AbstractService(String name, boolean enabled) {
        this.name = name;
        this.startEnabled = enabled;
        this.stopEnabled = enabled;
    }

    protected AbstractService(String name, boolean startEnabled, boolean stopEnabled) {
        this.name = name;
        this.startEnabled = startEnabled;
        this.stopEnabled = stopEnabled;
    }

    public final void start() {
        if (startEnabled) {
            ServiceFactory.LOG.info("[Start Service...] {}", this.name);
            try {
                doStart();
                ServiceFactory.LOG.info("[Start Service OK] {}", this.name);
            } catch (Throwable cause) {
                ServiceFactory.LOG.error("[x Start Service] " + name, cause);
            }
        }
    }

    public final void stop() {
        if (stopEnabled) {
            ServiceFactory.LOG.info("[Stop Service...] {}", this.name);
            try {
                doStop();
                ServiceFactory.LOG.info("[Stop Service OK] {}", this.name);
            } catch (Throwable cause) {
                ServiceFactory.LOG.error("[x Stop Service] " + name, cause);
            }
        }
    }

    protected abstract void doStart() throws Exception;
    protected abstract void doStop() throws Exception;
}
