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

import io.kindling.agent.deps.org.objectweb.asm.ClassVisitor;
import io.kindling.agent.deps.org.objectweb.asm.MethodVisitor;
import io.kindling.agent.deps.org.objectweb.asm.Opcodes;
import io.kindling.agent.deps.org.objectweb.asm.commons.JSRInlinerAdapter;
import io.kindling.agent.instrument.Jdk5ConvertClassVisitor;

public class AsmBridge {
    public static int ASM_API = Opcodes.ASM9;

    public static boolean ASM_TREE = true;

    public static ClassVisitor jsrAdapter(ClassVisitor cv) {
        return new Jdk5ConvertClassVisitor(cv);
    }

    public static MethodVisitor jsrAdapter(MethodVisitor mv, int access, String name, String desc, String signature, String[] exceptions) {
        return new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions);
    }
}
