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

import java.util.ArrayList;
import java.util.List;

public class MethodParamsFactory {
    public static MethodParamsMatcher createMethodParamsMatcher(String matchParamTypes) {
        if ("()".equals(matchParamTypes)) {
            return MethodParamsMatcher.EMPTY_MATCHER;
        }
        if ("(..)".equals(matchParamTypes)) {
            return MethodParamsMatcher.ANY_MATCHER;
        }
        return new CommonMethodParamsMatcher(matchParamTypes.substring(1, matchParamTypes.length() - 1).split(","));
    }
}
class CommonMethodParamsMatcher implements MethodParamsMatcher {
    List<MethodParamMatcher> methodParamMatchers = new ArrayList<MethodParamMatcher>();

    public CommonMethodParamsMatcher(String[] matchParamTypes) {
        methodParamMatchers = new ArrayList<MethodParamMatcher>();
        for (String matchParamType : matchParamTypes) {
            methodParamMatchers.add(new MethodParamMatcher(matchParamType));
        }
    }

    public boolean isMatch(String[] paramTypes) {
        if (paramTypes == null) {
            return false;
        }

        int j = 0;
        for (int i = 0; i < methodParamMatchers.size(); i++) {
            MethodParamMatcher matcher = methodParamMatchers.get(i);
            if (matcher.isVarArgType()) {
                if (i == methodParamMatchers.size() - 1) {
                    return true;
                } else {
                    /**
                     * 0 1 2 3 4 5 int java.lang.Object long int
                     * java.lang.String int int .. int
                     * 
                     * i: 1 paramTypes.length: 6 methodParamMatchers.size(): 3
                     * 
                     * j => 5
                     */
                    j = paramTypes.length - methodParamMatchers.size() + i + 1;
                    continue;
                }
            }

            if (j >= paramTypes.length || matcher.match(paramTypes[j]) == false) {
                return false;
            }
            j++;
        }
        if (j != paramTypes.length) {
            return false;
        }
        return true;
    }
}

enum MatchMethodParamType {
    VarArg, // ..
    PlaceHold, // *
    Prefix, // xxx*
    Suffix, // *xxx
    Equal; // xxx
}

class MethodParamMatcher {
    private final MatchMethodParamType type;
    private String keyword;

    public MethodParamMatcher(String paramType) {
        if ("..".equals(paramType)) {
            this.type = MatchMethodParamType.VarArg;
        } else if ("*".equals(paramType)) {
            this.type = MatchMethodParamType.PlaceHold;
        } else if (paramType.startsWith("*")) {
            this.type = MatchMethodParamType.Suffix;
            this.keyword = paramType.substring(1);
        } else if (paramType.endsWith("*")) {
            this.type = MatchMethodParamType.Prefix;
            this.keyword = paramType.substring(0, paramType.length() - 1);
        } else {
            this.type = MatchMethodParamType.Equal;
            this.keyword = paramType;
        }
    }

    public boolean match(String paramType) {
        switch (type) {
            case PlaceHold :
                return true;
            case Prefix :
                return paramType.startsWith(keyword);
            case Suffix :
                return paramType.endsWith(keyword);
            default :
                return paramType.equals(keyword);
        }
    }

    public boolean isVarArgType() {
        return MatchMethodParamType.VarArg.equals(type);
    }
}
