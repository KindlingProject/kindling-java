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

package io.kindling.agent.instrument.aspect.pointcut;

import io.kindling.agent.api.AdviceConfig;
import io.kindling.agent.api.Interceptor;
import io.kindling.agent.api.JoinPoint;

public class LoggingInterceptor implements Interceptor {
    public JoinPoint getJoinPoint() {
        return null;
    }

    public void before(JoinPoint joinPoint) {
    }

    public void after(JoinPoint joinPoint) {
    }

    public void afterThrowing(JoinPoint joinPoint) {
    }

    public boolean haveAdvice() {
        return false;
    }

    public boolean haveBeforeAdvice() {
        return false;
    }

    public boolean haveAfterAdvice() {
        return false;
    }

    public boolean haveAfterThrowingAdvice() {
        return false;
    }

    public AdviceConfig getAdviceConfig() {
        return null;
    }
}
