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

package io.kindling.agent.instrument.aspect.weave;

import io.kindling.agent.api.ClassStructure;
import io.kindling.agent.deps.org.objectweb.asm.ClassReader;
import io.kindling.agent.deps.org.objectweb.asm.ClassWriter;
import io.kindling.agent.deps.org.objectweb.asm.tree.ClassNode;
import io.kindling.agent.deps.org.objectweb.asm.util.CheckClassAdapter;
import io.kindling.agent.instrument.TransformUtil;
import io.kindling.agent.util.AsmBridge;

public class AsmApi {
    public static AsmResult weave(ClassLoader loader, ClassReader cr, ClassStructure classStructure) {
        ClassWriter cw = TransformUtil.newClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, loader, cr);
        ClassNode classNode = new ClassNode(AsmBridge.ASM_API);
        cr.accept(classNode, ClassReader.SKIP_FRAMES);

        AsmClass modifier = new AsmClass(classNode, classStructure);
        modifier.weave(loader);
        CheckClassAdapter ccv = new CheckClassAdapter(cw);
        modifier.getClassNode().accept(ccv);

        return new AsmResult(modifier.isModified(), cw.toByteArray());
    }
}
