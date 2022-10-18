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

package io.kindling.agent.instrument.aspect.matcher.method;

public class MethodNameFactory {
    public static MethodNameMatcher createMethodNameMatcher(String matchMethod) {
        if ("*".equals(matchMethod)) {
            return MethodNameMatcher.ALL_MATCHER;
        } else if (matchMethod.endsWith("*")) {
            return new SuffixFuzzyNameMatcher(matchMethod.substring(0, matchMethod.length() - 1));
        } else if (matchMethod.startsWith("*")) {
            return new PrefixFuzzyNameMatcher(matchMethod.substring(1, matchMethod.length()));
        } else {
            return new ExactNameMatcher(matchMethod);
        }
    }

    private static class PrefixFuzzyNameMatcher implements MethodNameMatcher {
        private final String suffix;

        public PrefixFuzzyNameMatcher(String suffix) {
            this.suffix = suffix;
        }

        public boolean isMatch(String methodName) {
            return methodName != null && methodName.endsWith(suffix);
        }
    }

    private static class SuffixFuzzyNameMatcher implements MethodNameMatcher {
        private final String prefix;

        public SuffixFuzzyNameMatcher(String prefix) {
            this.prefix = prefix;
        }

        public boolean isMatch(String methodName) {
            return methodName != null && methodName.startsWith(prefix);
        }
    }

    private static class ExactNameMatcher implements MethodNameMatcher {
        private final String mParam;

        public ExactNameMatcher(String mParam) {
            this.mParam = mParam;
        }

        public boolean isMatch(String methodName) {
            return this.mParam.equals(methodName);
        }
    }
}
