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

package io.kindling.agent.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {
    private final String name;
    private final boolean daemon;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public DefaultThreadFactory(String name, boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    public Thread newThread(Runnable runnable) {
        int num = threadNumber.getAndIncrement();
        String threadName = name;
        if (num != 1) {
            threadName += "-" + num;
        }
        Thread thread = new Thread(runnable, threadName);
        if (daemon) {
            thread.setDaemon(true);
        }
        return thread;
    }
}
