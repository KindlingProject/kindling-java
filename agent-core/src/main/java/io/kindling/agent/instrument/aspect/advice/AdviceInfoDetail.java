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

import io.kindling.agent.api.Advice;
import io.kindling.agent.api.AfterAdvice;
import io.kindling.agent.api.AroundAdvice;
import io.kindling.agent.api.BeforeAdvice;

public class AdviceInfoDetail {
    private String adviceName;
    private Advice advice;

    private BeforeAdvice beforeAdvice;
    private AfterAdvice afterAdvice;
    private boolean isThrowableAdvice;

    public AdviceInfoDetail(Advice advice, String adviceName) {
        this.adviceName = adviceName;
        this.advice = advice;

        if (advice instanceof BeforeAdvice) {
            this.beforeAdvice = (BeforeAdvice) this.advice;
        }
        if (advice instanceof AfterAdvice) {
            this.afterAdvice = (AfterAdvice) this.advice;
        }
        if (advice instanceof AroundAdvice) {
            advice.getAdviceConfig().enableExceptionParam();
        }
        this.isThrowableAdvice = afterAdvice != null && advice.getAdviceConfig().isExceptionEnabled();
    }

    public String getAdviceName() {
        return adviceName;
    }

    public Advice getAdvice() {
        return advice;
    }

    public BeforeAdvice getBeforeAdvice() {
        return beforeAdvice;
    }

    public AfterAdvice getAfterAdvice() {
        return afterAdvice;
    }

    public boolean isThrowableAdvice() {
        return isThrowableAdvice;
    }
}
