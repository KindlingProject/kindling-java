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

package io.kindling.agent.api;

import java.util.LinkedList;

public class KindlingSpan {
    private static ThreadLocal<LinkedList<KindlingSpan>> LOCAL_SPAN = new ThreadLocal<LinkedList<KindlingSpan>>() {
        protected LinkedList<KindlingSpan> initialValue() {
            return new LinkedList<KindlingSpan>();
        };
    };

    private final long startTime;
    private final String name;
    
    public KindlingSpan(String name) {
        this.startTime = System.nanoTime();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getDuration() {
        return System.nanoTime() - startTime;
    }

    public static void start(String name) {
        LinkedList<KindlingSpan> activeSpans = LOCAL_SPAN.get();
        if (activeSpans == null || activeSpans.size() > 256) {
            return;
        }
        /**
         * Ignore Same Name. Eg. Skywalking Entry(Tomcat / SpringMVC)
         */
        if (activeSpans.size() > 0 && name.equals(activeSpans.peek().getName())) {
            return;
        }
        activeSpans.push(new KindlingSpan(name));
    }

    public static void stop(String traceId, String name) {
        LinkedList<KindlingSpan> activeSpans = LOCAL_SPAN.get();
        if (activeSpans == null || activeSpans.isEmpty()) {
            return;
        }
        if (name.equals(activeSpans.peek().getName()) == false) {
            return;
        }
        KindlingApi.recordSpan(traceId, name, activeSpans.pop().getDuration());
    }
}