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

import java.util.List;

import io.kindling.agent.api.AdviceConfig;
import io.kindling.agent.api.Interceptor;
import io.kindling.agent.api.JoinPoint;
import io.kindling.agent.api.MethodSignature;
import io.kindling.agent.exception.AdviceInitException;
import io.kindling.agent.service.ServiceFactory;

public class MultiAdvicesInterceptor implements Interceptor {
    private static final int MAX_MULTI_ADVICE_COUNT = 31;

    private MethodSignature methodSignature;
    private List<AdviceInfoDetail> adviceInfoDetails;
    private int adviceNum;
    private AdviceConfig adviceConfig;

    public MultiAdvicesInterceptor(MethodSignature methodSignature, List<AdviceInfoDetail> adviceInfoList) throws AdviceInitException {
        this.methodSignature = methodSignature;
        this.adviceInfoDetails = adviceInfoList;
        this.adviceNum = adviceInfoList.size();
        this.adviceConfig = new AdviceConfig();
        for (AdviceInfoDetail adviceInfoDetail : adviceInfoList) {
            AdviceConfig otherConfig = adviceInfoDetail.getAdvice().getAdviceConfig();
            if (otherConfig.isThisEnabled() && methodSignature.isStaticMethod()) {
                ServiceFactory.LOG.info("[Ignore Advice] {} for method {} by Can't get this variable in static method.",
                    adviceInfoDetail.getAdviceName(), methodSignature.getSignature());
            } else if (methodSignature.getArgNum() <= otherConfig.getMaxArgIndex()) {
                ServiceFactory.LOG.info("[Ignore Advice] {} for method {} by Advice argument index {} is larger than method argument size.",
                    adviceInfoDetail.getAdviceName(), methodSignature.getSignature(), otherConfig.getMaxArgIndex());
            } else {
                adviceConfig.mergeConfig(otherConfig);
            }
        }

        if (adviceInfoList.isEmpty()) {
            throw new AdviceInitException("[Ignore Advice] No valide advice for " + methodSignature.getSignature());
        }
        if (adviceConfig.isArgsEnabled() && adviceConfig.isArgEnabled()) {
            ServiceFactory.LOG.info("[Ignore Advice] Bad Advice Config for both set arg and args for {}", methodSignature.getSignature());
        }
    }

    public JoinPoint getJoinPoint() {
        if (adviceInfoDetails.size() < MAX_MULTI_ADVICE_COUNT) {
            int acceptAdviceIndexes = 0;
            for (int i = 0; i < adviceNum; i++) {
                acceptAdviceIndexes |= (1 << i);
            }
            if (acceptAdviceIndexes == 0) {
                return null;
            }
            return new MultiJoinPointImpl(acceptAdviceIndexes);
        } else {
            ServiceFactory.LOG.error("[Ignore Advice] " + methodSignature.getSignature() + " by exceed max.");
            return null;
        }
    }

    public void before(JoinPoint joinPoint) {
        try {
            int acceptAdviceIndexes = joinPoint.getAcceptAdviceIndexes();
            for (int index = 0; index < adviceNum; index++) {
                if ((acceptAdviceIndexes & (1 << index)) != 0) {
                    AdviceInfoDetail adviceInfoDetail = adviceInfoDetails.get(index);
                    if (adviceInfoDetail.getBeforeAdvice() != null) {
                        try {
                            adviceInfoDetail.getBeforeAdvice().before(joinPoint);
                        } catch (Throwable cause) {
                            ServiceFactory.LOG.error("[x Call Before] " + adviceInfoDetail.getAdviceName(), cause);
                        }
                    }
                }
            }
        } catch (Throwable cause) {
            ServiceFactory.LOG.error("[x Call Before] " + methodSignature.getSignature(), cause);
        }
    }

    public void after(JoinPoint joinPoint) {
        try {
            int acceptAdviceIndexes = joinPoint.getAcceptAdviceIndexes();
            for (int index = adviceNum - 1; index >= 0; index--) {
                if ((acceptAdviceIndexes & (1 << index)) != 0) {
                    AdviceInfoDetail adviceInfoDetail = adviceInfoDetails.get(index);
                    if (adviceInfoDetail.getAfterAdvice() != null) {
                        try {
                            adviceInfoDetail.getAfterAdvice().after(joinPoint);
                        } catch (Throwable cause) {
                            ServiceFactory.LOG.error("[x Call After] " + adviceInfoDetail.getAdviceName(), cause);
                        }
                    }
                }
            }
        } catch (Throwable cause) {
            ServiceFactory.LOG.error("[x Call After] " + methodSignature.getSignature(), cause);
        }
    }

    public void afterThrowing(JoinPoint joinPoint) {
        try {
            int acceptAdviceIndexes = joinPoint.getAcceptAdviceIndexes();
            for (int index = adviceNum - 1; index >= 0; index--) {
                if ((acceptAdviceIndexes & (1 << index)) != 0) {
                    AdviceInfoDetail adviceInfoDetail = adviceInfoDetails.get(index);
                    if (adviceInfoDetail.isThrowableAdvice()) {
                        try {
                            adviceInfoDetail.getAfterAdvice().after(joinPoint);
                        } catch (Throwable cause) {
                            ServiceFactory.LOG.error("[x Call AfterThrow] " + adviceInfoDetail.getAdviceName(), cause);
                        }
                    }
                }
            }
        } catch (Throwable cause) {
            ServiceFactory.LOG.error("[x Call AfterThrow] " + methodSignature.getSignature(), cause);
        }
    }

    public boolean haveAdvice() {
        return adviceInfoDetails.size() > 0;
    }

    public boolean haveBeforeAdvice() {
        for (AdviceInfoDetail adviceInfoDetail : adviceInfoDetails) {
            if (adviceInfoDetail.getBeforeAdvice() != null) {
                return true;
            }
        }
        return false;
    }

    public boolean haveAfterAdvice() {
        for (AdviceInfoDetail adviceInfoDetail : adviceInfoDetails) {
            if (adviceInfoDetail.getAfterAdvice() != null) {
                return true;
            }
        }
        return false;
    }

    public boolean haveAfterThrowingAdvice() {
        for (AdviceInfoDetail adviceInfoDetail : adviceInfoDetails) {
            if (adviceInfoDetail.isThrowableAdvice()) {
                return true;
            }
        }
        return false;
    }

    public AdviceConfig getAdviceConfig() {
        return adviceConfig;
    }
}
