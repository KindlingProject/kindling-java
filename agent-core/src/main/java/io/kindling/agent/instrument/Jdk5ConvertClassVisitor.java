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

package io.kindling.agent.instrument;

import io.kindling.agent.deps.org.objectweb.asm.ClassVisitor;
import io.kindling.agent.deps.org.objectweb.asm.MethodVisitor;
import io.kindling.agent.deps.org.objectweb.asm.Opcodes;
import io.kindling.agent.deps.org.objectweb.asm.commons.JSRInlinerAdapter;
import io.kindling.agent.util.AsmBridge;

//FIX [JSR/RET are not supported with computeFrames option]
public class Jdk5ConvertClassVisitor extends ClassVisitor {
 public Jdk5ConvertClassVisitor(ClassVisitor cv) {
     super(AsmBridge.ASM_API, cv);
 }

 public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
     if (version < Opcodes.V1_5 || version > 100) {
         int newVersion = Opcodes.V1_6;
         version = newVersion;
     }
     super.visit(version, access, name, signature, superName, interfaces);
 }
 
 public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
     return new JSRInlinerAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc, signature, exceptions);
 }
}
