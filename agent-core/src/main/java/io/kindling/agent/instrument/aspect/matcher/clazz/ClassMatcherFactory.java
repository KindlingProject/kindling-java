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

package io.kindling.agent.instrument.aspect.matcher.clazz;

import io.kindling.agent.api.ClassStructure;

public class ClassMatcherFactory {
    private static ClassMatcherFactory instance = new ClassMatcherFactory();

    private ClassMatcherFactory() {
    }

    public static ClassMatcherFactory getInstance() {
        return instance;
    }

    public ClassMatcher createClassMatcher(String matchClass) {
        if (matchClass.startsWith("@")) {
            return new AnnotatedMatcher(matchClass);
        } else if (matchClass.endsWith("+")) {
            return new ChildMatcher(matchClass);
        } else if (matchClass.endsWith("*")) {
            return new PrefixMatcher(matchClass);
        } else {
            return new ExactMatcher(matchClass);
        }
    }

    private class AnnotatedMatcher implements ClassMatcher {
        private final String matchClass;
        private final String annotatedClass;

        public AnnotatedMatcher(String className) {
            this.matchClass = className;
            this.annotatedClass = className.substring(1, className.length());
        }

        public boolean isMatch(ClassStructure classStructure) {
            return classStructure.getAnnotationTypes().contains(annotatedClass);
        }

        public String getMatchClass() {
            return matchClass;
        }
    }

    private class ChildMatcher implements ClassMatcher {
        private final String matchClass;
        private final String superClassName;

        public ChildMatcher(String className) {
            this.matchClass = className;
            this.superClassName = className.substring(0, className.length() - 1);
        }

        public boolean isMatch(ClassStructure classStructer) {
            return classStructer.isChild(superClassName);
        }

        public String getMatchClass() {
            return matchClass;
        }
    }

    private class PrefixMatcher implements ClassMatcher {
        private final String matchClass;
        private final String prefix;

        public PrefixMatcher(String className) {
            this.matchClass = className;
            this.prefix = className.substring(0, className.length() - 1);
        }

        public boolean isMatch(ClassStructure classStructer) {
            return classStructer.getJavaClassName().startsWith(prefix);
        }

        public String getMatchClass() {
            return matchClass;
        }
    }

    private class ExactMatcher implements ClassMatcher {
        private final String clazzName;

        public ExactMatcher(String clazzName) {
            this.clazzName = clazzName;
        }

        public boolean isMatch(ClassStructure classStructer) {
            return classStructer.getJavaClassName().equals(this.clazzName);
        }

        public String getMatchClass() {
            return clazzName;
        }
    }
}
