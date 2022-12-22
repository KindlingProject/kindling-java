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

package io.kindling.plugin.span.pp;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.profiler.context.DefaultTraceContext;
import com.navercorp.pinpoint.profiler.metadata.DefaultApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.Result;
import com.navercorp.pinpoint.profiler.metadata.SimpleCache;

import io.kindling.agent.service.ICallback;
import io.kindling.agent.service.ServiceFactory;
import io.kindling.agent.util.Reflection;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

public class PpApiCache {
    private static AtomicBoolean init = new AtomicBoolean(false);
    private static Map<Integer, String> ppApiCache = new ConcurrentHashMap<Integer, String>();

    public static void cache(int apiId, MethodDescriptor methodDescriptor) {
        ppApiCache.put(apiId, methodDescriptor.getFullName());
    }

    public static void cache(DefaultTraceContext traceContext) {
        DefaultApiMetaDataService apiMetaDataService = (DefaultApiMetaDataService) Reflection.getField(traceContext, "apiMetaDataService");
        cache(apiMetaDataService);
    }

    public static boolean checkAndCache() {
        if (init.compareAndSet(false, true)) {
            ServiceFactory.CACHE.registryCache("PpApiCache", new ICallback() {
                public void callback() {
                    PpApiCache.reset();
                }
            });
            return true;
        } else {
            return false;
        }
    }

    public static void cache(DefaultApiMetaDataService apiMetaDataService) {
        if (apiMetaDataService == null) {
            return;
        }
        SimpleCache<String> apiCache = (SimpleCache<String>) Reflection.getField(apiMetaDataService, "apiCache");
        if (apiCache == null) {
            return;
        }
        ConcurrentMap<String, Result> cache = (ConcurrentMap<String, Result>) Reflection.getField(apiCache, "cache");
        if (cache == null) {
            return;
        }
        for (Map.Entry<String, Result> entry : cache.entrySet()) {
            ppApiCache.put(entry.getValue().getId(), entry.getKey());
        }
    }

    public static String getApiName(int apiId) {
        return ppApiCache.get(apiId);
    }

    public static void reset() {
        ppApiCache.clear();
        init.set(false);
    }
}
