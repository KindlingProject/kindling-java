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

package io.kindling.agent.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Reflection {
    public static boolean hasMethod(Class<?> clazz, String methodName) {
        for (Class<?> searchType = clazz; searchType != Object.class; searchType = searchType.getSuperclass()) {
            try {
                Method[] methods = searchType.getDeclaredMethods();
                for (Method method : methods) {
                    if (methodName.equals(method.getName())) {
                        return true;
                    }
                }
            } catch (Exception cause) {
            }
        }
        return false;
    }

    public static Object getField(Object obj, String fieldName) {
        Field field = getAccessibleField(obj.getClass(), fieldName);
        if (field == null) {
            return null;
        }
        try {
            return field.get(obj);
        } catch (Throwable cause) {
            return null;
        }
    }

    public static Field getAccessibleField(Class<?> clazz, String fieldName) {
        for (Class<?> searchType = clazz; searchType != Object.class; searchType = searchType.getSuperclass()) {
            try {
                Field field = searchType.getDeclaredField(fieldName);
                makeAccessible(field);
                return field;
            } catch (Exception cause) {
            }
        }
        return null;
    }

    private static void makeAccessible(Field field) {
        if (field.isAccessible() == false && (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()))) {
            field.setAccessible(true);
        }
    }
}
