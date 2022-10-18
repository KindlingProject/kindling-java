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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.kindling.agent.api.Advice;
import io.kindling.agent.api.MethodSignature;
import io.kindling.agent.exception.AspectInitException;
import io.kindling.agent.instrument.classloader.ExtendLoader;
import io.kindling.agent.service.ServiceFactory;

public class AdviceFactory {
    /**
     * Fetch a advice.the instance can be cached
     * 
     * @param adviceClassName The advice's class name
     * @return A advice instance
     * @throws ClassNotFoundException when the class of advice not found.
     * @throws InstantiationException when instantiation error.
     * @throws IllegalAccessException when illegalAccess occor.
     * @throws AspectInitException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     */
    public static AdviceInfoDetail getAdviceInfo(ClassLoader loader, MethodSignature methodSignature, String adviceClassName) throws ClassNotFoundException,
        InstantiationException, IllegalAccessException, AspectInitException, IllegalArgumentException, InvocationTargetException {
        
        Class<?> adviceClass = ExtendLoader.loadPlugin(adviceClassName, loader);
        ServiceFactory.LOG.info("New AdviceClass: {}, ClassLoader: {}", adviceClassName, adviceClass.getClassLoader().getClass().getName());

        Advice advice = (Advice) newAdvice(adviceClass, methodSignature);
        return new AdviceInfoDetail(advice, adviceClassName);
    }
    
    private static Object newAdvice(Class<?> adviceClass, MethodSignature methodSignature)
            throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final Constructor<?>[] constructors = adviceClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 0) {
                return constructor.newInstance();
            } else if (parameterTypes.length == 1 && MethodSignature.class.equals(parameterTypes[0])) {
                return constructor.newInstance(new Object[] {methodSignature});
            }
        }
        throw new IllegalArgumentException("Bad Consturctor for advice: " + adviceClass.getName());
    }
}
