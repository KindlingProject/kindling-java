/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultApiMetaDataService implements ApiMetaDataService {
    private final SimpleCache<String> apiCache;

    public DefaultApiMetaDataService(SimpleCache<String> apiCache) {
        this.apiCache = apiCache;
    }

    public int cacheApi(final MethodDescriptor methodDescriptor) {
        final String fullName = methodDescriptor.getFullName();
        final Result result = this.apiCache.put(fullName);

        methodDescriptor.setApiId(result.getId());
        return result.getId();
    }
}
