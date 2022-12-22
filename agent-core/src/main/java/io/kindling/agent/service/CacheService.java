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

import java.util.Map;
import java.util.HashMap;

public class CacheService implements ICacheService {
    private Map<String, ICallback> cleanTasks = new HashMap<String, ICallback>();
    
    public void registryCache(String name, ICallback callback) {
        cleanTasks.put(name, callback);
    }

    public void cleanCache() {
        for (Map.Entry<String, ICallback> entry : cleanTasks.entrySet()) {
            entry.getValue().callback();
            ServiceFactory.LOG.info("[Clean Cache] {}", entry.getKey());
        }
    }
}
