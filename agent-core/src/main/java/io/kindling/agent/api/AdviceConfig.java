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

public class AdviceConfig {
    private static final short PARAM_THIS = 0x01;
    private static final short PARAM_ARGS = 0x02;
    private static final short PARAM_EXCEPTION = 0x04;
    private static final short PARAM_RETURNOBJECT = 0x08;
    private static final short PARAM_ARG = 0x10;

    private short paramSwtich;
    private boolean arg0Enabled;
    private boolean arg1Enabled;
    private boolean arg2Enabled;

    public AdviceConfig() {
    }

    public AdviceConfig(short paramSwtich) {
        this.paramSwtich = paramSwtich;
    }

    public AdviceConfig enableThisParam() {
        paramSwtich |= PARAM_THIS;
        return this;
    }

    public AdviceConfig enableArgsParam() {
        paramSwtich |= PARAM_ARGS;
        return this;
    }

    public AdviceConfig enableExceptionParam() {
        paramSwtich |= PARAM_EXCEPTION;
        return this;
    }

    public AdviceConfig enableReturnObjectParam() {
        paramSwtich |= PARAM_RETURNOBJECT;
        return this;
    }

    public AdviceConfig enableArg0Param() {
        this.paramSwtich |= PARAM_ARG;
        this.arg0Enabled = true;
        return this;
    }

    public AdviceConfig enableArg1Param() {
        this.paramSwtich |= PARAM_ARG;
        this.arg1Enabled = true;
        return this;
    }

    public AdviceConfig enableArg2Param() {
        this.paramSwtich |= PARAM_ARG;
        this.arg2Enabled = true;
        return this;
    }

    public void mergeConfig(AdviceConfig config) {
        this.paramSwtich |= config.getParamSwtich();
        this.arg0Enabled |= config.isArg0Enabled();
        this.arg1Enabled |= config.isArg1Enabled();
        this.arg2Enabled |= config.isArg2Enabled();
    }

    public int getParamSwtich() {
        return paramSwtich;
    }

    public boolean isThisEnabled() {
        return (paramSwtich & PARAM_THIS) > 0;
    }

    public boolean isArgsEnabled() {
        return (paramSwtich & PARAM_ARGS) > 0;
    }

    public boolean isArgEnabled() {
        return (paramSwtich & PARAM_ARG) > 0;
    }

    public boolean isExceptionEnabled() {
        return (paramSwtich & PARAM_EXCEPTION) > 0;
    }

    public boolean isReturnObjectEnabled() {
        return (paramSwtich & PARAM_RETURNOBJECT) > 0;
    }

    public boolean isArg0Enabled() {
        return arg0Enabled;
    }

    public boolean isArg1Enabled() {
        return arg1Enabled;
    }

    public boolean isArg2Enabled() {
        return arg2Enabled;
    }

    public int getMaxArgIndex() {
        if (arg2Enabled) {
            return 2;
        } else if (arg1Enabled) {
            return 1;
        } else if (arg0Enabled) {
            return 0;
        }
        return -1;
    }
}
