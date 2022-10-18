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

import java.util.HashMap;
import java.util.Map;

import io.kindling.agent.api.JoinPoint;

public class MultiJoinPointImpl implements JoinPoint {
    private Object that;
    private Object[] args;
    private Object arg0;
    private Object arg1;
    private Object arg2;
    private Throwable exception;
    private Object returnObject;
    private Map<Integer, Object> varArg;
    private boolean returnImmediately;
    private Object replaceValue;

    private int acceptAdviceIndexes;

    public MultiJoinPointImpl(int acceptAdviceIndexes) {
        this.acceptAdviceIndexes = acceptAdviceIndexes;
    }

    public Object getThis() {
        return this.that;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object getArg0() {
        return arg0;
    }

    public Object getArg1() {
        return arg1;
    }

    public Object getArg2() {
        return arg2;
    }

    public Object getVarArg(int index) {
        return varArg == null ? null : varArg.get(index);
    }

    public boolean isReturnImmediately() {
        return returnImmediately;
    }

    public Object getReplaceValue() {
        return replaceValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnImmediately = true;
        this.replaceValue = returnValue;
    }

    public Throwable getException() {
        return exception;
    }

    public Object getReturnObject() {
        return returnObject;
    }

    public void setThat(Object that) {
        this.that = that;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public void setArg0(Object arg0) {
        this.arg0 = arg0;
    }

    public void setArg1(Object arg1) {
        this.arg1 = arg1;
    }

    public void setArg2(Object arg2) {
        this.arg2 = arg2;
    }

    public void setVarArg(int index, Object object) {
        if (varArg == null) {
            varArg = new HashMap<Integer, Object>();
        }
        this.varArg.put(index, object);
    }

    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    public int getAcceptAdviceIndexes() {
        return acceptAdviceIndexes;
    }
}
