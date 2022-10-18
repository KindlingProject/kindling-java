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

package io.kindling.agent.instrument.aspect.advice;

import io.kindling.agent.api.AdviceConfig;
import io.kindling.agent.api.AfterAdvice;
import io.kindling.agent.api.BeforeAdvice;
import io.kindling.agent.api.Interceptor;
import io.kindling.agent.api.JoinPoint;
import io.kindling.agent.api.MethodSignature;
import io.kindling.agent.exception.AdviceInitException;
import io.kindling.agent.service.ServiceFactory;

public class SingleAdviceInterceptor implements Interceptor {
    private AdviceInfoDetail adviceInfoDetail;

    private BeforeAdvice beforeAdvice;
    private AfterAdvice afterAdvice;
    private boolean isThrowableAdvice;
    private AdviceConfig adviceConfig;

    public SingleAdviceInterceptor(MethodSignature methodSignature, AdviceInfoDetail adviceInfo) throws AdviceInitException {
        this.adviceInfoDetail = adviceInfo;

        this.adviceConfig = adviceInfo.getAdvice().getAdviceConfig();
        this.beforeAdvice = adviceInfo.getBeforeAdvice();
        this.afterAdvice = adviceInfo.getAfterAdvice();
        this.isThrowableAdvice = adviceInfo.isThrowableAdvice();

        if (adviceConfig.isThisEnabled() && methodSignature.isStaticMethod()) {
            throw new AdviceInitException("Can't get this variable in static method for method " + methodSignature.getSignature());
        }
        if (adviceConfig.isArgEnabled() && adviceConfig.isArgsEnabled()) {
            throw new AdviceInitException("Cann't both set arg and args for method " + methodSignature.getSignature());
        }
        if (adviceConfig.isArgEnabled() && methodSignature.getArgNum() <= adviceConfig.getMaxArgIndex()) {
            throw new AdviceInitException("Advice argument index " + adviceConfig.getMaxArgIndex() + " is larger than method argument size for method " + methodSignature.getSignature());
        }
    }

    public JoinPoint getJoinPoint() {
        return new JoinPointImpl();
    }

    public void before(JoinPoint joinPoint) {
        try {
            beforeAdvice.before(joinPoint);
        } catch (Throwable cause) {
            ServiceFactory.LOG.error("[x Call Before] " + adviceInfoDetail.getAdviceName(), cause);
        }
    }

    public void after(JoinPoint joinPoint) {
        try {
            afterAdvice.after(joinPoint);
        } catch (Throwable cause) {
            ServiceFactory.LOG.error("[x Call After] " + adviceInfoDetail.getAdviceName(), cause);
        }
    }

    public void afterThrowing(JoinPoint joinPoint) {
        try {
            afterAdvice.after(joinPoint);
        } catch (Throwable cause) {
            ServiceFactory.LOG.error("[x Call AfterThrow] " + adviceInfoDetail.getAdviceName(), cause);
        }
    }

    public boolean haveAdvice() {
        return beforeAdvice != null || afterAdvice != null;
    }

    public boolean haveBeforeAdvice() {
        return beforeAdvice != null;
    }

    public boolean haveAfterAdvice() {
        return afterAdvice != null;
    }

    public boolean haveAfterThrowingAdvice() {
        return isThrowableAdvice;
    }

    public AdviceConfig getAdviceConfig() {
        return adviceConfig;
    }
}
