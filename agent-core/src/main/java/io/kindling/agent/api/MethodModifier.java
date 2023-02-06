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

import io.kindling.agent.deps.org.objectweb.asm.Opcodes;

public enum MethodModifier {
    NONE_BRIDGE(
        "!bridge",
        0,
        Opcodes.ACC_BRIDGE
    ),
    PUBLIC(
        "public",
        Opcodes.ACC_PUBLIC,
        0
    ),
    PRIVATE(
        "private",
        Opcodes.ACC_PRIVATE,
        0
    ),
    PROTCTED(
        "protected",
        Opcodes.ACC_PROTECTED,
        0
    ),
    ALL(
        "*",
        0,
        0
    );
    
    private final String key;
    private final int matchAccess;
    private final int notMatchAccess;
    
    private MethodModifier(String key, int matchAccess, int notMatchAccess) {
        this.key = key;
        this.matchAccess = matchAccess;
        this.notMatchAccess = notMatchAccess;
    }

    public static MethodModifier of(String type) {
        for (MethodModifier modifier : MethodModifier.values()) {
            if (modifier.name().equalsIgnoreCase(type)) {
                return modifier;
            }
        }
        return ALL;
    }
    
    public boolean isMatch(int methodAccess) {
        if (matchAccess > 0 && (matchAccess & methodAccess) == 0) {
            return false;
        }
        if (notMatchAccess > 0 && (notMatchAccess & methodAccess) != 0) {
            return false;
        }
        return true;
    }

    public String getDesc() {
        return key;
    }
}
