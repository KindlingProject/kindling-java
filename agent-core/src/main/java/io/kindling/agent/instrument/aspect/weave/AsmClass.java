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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.kindling.agent.api.AdviceConfig;
import io.kindling.agent.api.ClassStructure;
import io.kindling.agent.api.Interceptor;
import io.kindling.agent.api.JoinPoint;
import io.kindling.agent.api.MethodSignature;
import io.kindling.agent.deps.org.objectweb.asm.ClassVisitor;
import io.kindling.agent.deps.org.objectweb.asm.MethodVisitor;
import io.kindling.agent.deps.org.objectweb.asm.Opcodes;
import io.kindling.agent.deps.org.objectweb.asm.Type;
import io.kindling.agent.deps.org.objectweb.asm.commons.Method;
import io.kindling.agent.deps.org.objectweb.asm.tree.AbstractInsnNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.ClassNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.InsnList;
import io.kindling.agent.deps.org.objectweb.asm.tree.LabelNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.LocalVariableNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.MethodInsnNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.MethodNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.TryCatchBlockNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.TypeInsnNode;
import io.kindling.agent.instrument.aspect.pointcut.AspectRegistry;
import io.kindling.agent.util.AsmBridge;

public class AsmClass {
    private static final Type TYPE_ASPECTREGISTRY = Type.getType(AspectRegistry.class);
    private static final Method METHOD_GETINTERCEPTOR = new Method("getInterceptor", "(I)Lio/kindling/agent/api/Interceptor;");

    private static final Type TYPE_INTERCEPTOR = Type.getType(Interceptor.class);
    private static final Method METHOD_GETJOINPOINT = new Method("getJoinPoint", "()Lio/kindling/agent/api/JoinPoint;");
    private static final Method METHOD_BEFORE = new Method("before", "(Lio/kindling/agent/api/JoinPoint;)V");
    private static final Method METHOD_AFTER = new Method("after", "(Lio/kindling/agent/api/JoinPoint;)V");
    private static final Method METHOD_AFTERTHROWING = new Method("afterThrowing", "(Lio/kindling/agent/api/JoinPoint;)V");

    private static final Type TYPE_JOINPOINT = Type.getType(JoinPoint.class);
    private static final Method METHOD_SETTHAT = new Method("setThat", "(Ljava/lang/Object;)V");
    private static final Method METHOD_SETARGS = new Method("setArgs", "([Ljava/lang/Object;)V");
    private static final Method METHOD_SETARG0 = new Method("setArg0", "(Ljava/lang/Object;)V");
    private static final Method METHOD_SETARG1 = new Method("setArg1", "(Ljava/lang/Object;)V");
    private static final Method METHOD_SETARG2 = new Method("setArg2", "(Ljava/lang/Object;)V");
    private static final Method METHOD_SETEXCEPTION = new Method("setException", "(Ljava/lang/Throwable;)V");
    private static final Method METHOD_SETRETURNOBJECT = new Method("setReturnObject", "(Ljava/lang/Object;)V");

    private static final Type TYPE_THROWABLE = Type.getType(Throwable.class);

    private final ClassStructure classStructure;
    private final ClassNode classNode;
    private boolean modified = false;

    public AsmClass(ClassNode classNode, ClassStructure classStructure) {
        this.classStructure = classStructure;
        ClassNode result = new ClassNode(AsmBridge.ASM_API);
        classNode.accept(new ClassVisitor(AsmBridge.ASM_API, result) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                return AsmBridge.jsrAdapter(mv, access, name, desc, signature, exceptions);
            }
        });
        this.classNode = result;
    }

    public void weave(ClassLoader loader) {
        for (Object node : classNode.methods) {
            MethodNode methodNode = (MethodNode) node;
            if ((methodNode.access & Opcodes.ACC_NATIVE) == 0 && (methodNode.access & Opcodes.ACC_ABSTRACT) == 0) {

                MethodSignature methodSignature = new MethodSignature(classStructure.getJavaClassName(), methodNode.access, methodNode.name, methodNode.desc);
                int interceptorId = AspectRegistry.addInterceptor(classStructure, loader, methodSignature);

                if (interceptorId >= 0) {
                    new AsmMethod(methodNode).addInterceptor(AspectRegistry.getInterceptor(interceptorId), interceptorId);
                    modified = true;
                }
            }
        }
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public boolean isModified() {
        return modified;
    }

    class AsmMethod {
        private MethodNode methodNode;

        private boolean isStatic;
        private final Type[] argumentTypes;
        private final Type returnType;

        private int nextLocals;

        private AbstractInsnNode enterInsnNode;
        private AbstractInsnNode exitInsnNode;

        private final LabelNode interceptorVariableStartLabelNode = new LabelNode();
        private final LabelNode interceptorVariableEndLabelNode = new LabelNode();

        private String interceptorVariableName = "_$hc_interceptor$_";
        private LocalVariableNode interceptorVariableNode = null;

        private String joinPointVariableName = "_$hc_joinpoint$_";
        private LocalVariableNode joinPointVariableNode = null;

        private String throwVariableName = "_$hc_throw$_";
        private LocalVariableNode throwVariableNode = null;

        public AsmMethod(final MethodNode methodNode) {
            this.methodNode = methodNode;
            this.nextLocals = methodNode.maxLocals;
            this.argumentTypes = Type.getArgumentTypes(methodNode.desc);
            this.returnType = Type.getReturnType(methodNode.desc);
            this.isStatic = AsmUtil.isStatic(methodNode);

            // find enter & exit instruction.
            if (AsmUtil.isConstructor(methodNode)) {
                this.enterInsnNode = findInitConstructorInstruction();
            } else {
                this.enterInsnNode = methodNode.instructions.getFirst();
            }
            // when the method is empty, both enterInsnNode and lastInsnNode are
            // Opcodes.RETURN ;
            this.exitInsnNode = methodNode.instructions.getLast();

            this.methodNode.instructions.insertBefore(this.enterInsnNode, this.interceptorVariableStartLabelNode);
            this.methodNode.instructions.insert(this.exitInsnNode, this.interceptorVariableEndLabelNode);
        }

        public void addInterceptor(Interceptor interceptor, int interceptorId) {
            if (interceptor != null && interceptor.haveAdvice()) {
                InsnList toInsert = new InsnList();
                this.interceptorVariableNode = addInterceptorLocalVariable(interceptorVariableName, TYPE_INTERCEPTOR);
                this.joinPointVariableNode = addInterceptorLocalVariable(joinPointVariableName, TYPE_JOINPOINT);

                // Interceptor interceptor = AspectRegistry.getInterceptor(xxx);
                AsmUtil.push(toInsert, interceptorId);
                AsmUtil.invokeStatic(toInsert, TYPE_ASPECTREGISTRY, METHOD_GETINTERCEPTOR);
                AsmUtil.storeLocal(toInsert, TYPE_INTERCEPTOR, interceptorVariableNode.index);

                // JoinPointImpl joinPoint = interceptor.getJoinPoint();
                loadInterceptor(toInsert);
                AsmUtil.invokeInterface(toInsert, TYPE_INTERCEPTOR, METHOD_GETJOINPOINT);
                AsmUtil.storeLocal(toInsert, TYPE_JOINPOINT, joinPointVariableNode.index);

                AdviceConfig adviceConfig = interceptor.getAdviceConfig();
                boolean needCheck = ((adviceConfig.isThisEnabled() && !isStatic) || adviceConfig.isArgsEnabled() || adviceConfig.isArgEnabled() || interceptor.haveBeforeAdvice());
                if (needCheck) {
                    before(toInsert, interceptor, adviceConfig);
                }
                this.methodNode.instructions.insertBefore(enterInsnNode, toInsert);
            }

            ASMTryCatch tryCatch = null;
            if (interceptor.haveAfterThrowingAdvice()) {
                tryCatch = new ASMTryCatch(methodNode);
                this.methodNode.instructions.insertBefore(enterInsnNode, tryCatch.getStartLabelNode());
                this.methodNode.instructions.insert(exitInsnNode, tryCatch.getEndLabelNode());
            }

            // find return.
            if (interceptor != null && interceptor.haveAfterAdvice()) {
                AbstractInsnNode insnNode = this.methodNode.instructions.getFirst();
                while (insnNode != null) {
                    final int opcode = insnNode.getOpcode();
                    if (AsmUtil.isReturnCode(opcode)) {
                        final InsnList instructions = new InsnList();
                        after(instructions, interceptor, opcode);
                        this.methodNode.instructions.insertBefore(insnNode, instructions);
                    }
                    insnNode = insnNode.getNext();
                }
            }

            if (tryCatch != null) {
                // try catch handler.
                InsnList instructions = new InsnList();
                afterThrowing(instructions, interceptor);
                this.methodNode.instructions.insert(tryCatch.getEndLabelNode(), instructions);
                tryCatch.sort();
            }
            methodNode.maxStack = methodNode.maxStack + 8;
        }

        private LocalVariableNode initThrowableVariableNode() {
            if (throwVariableNode == null) {
                throwVariableNode = addInterceptorLocalVariable(throwVariableName, TYPE_THROWABLE);
            }
            return throwVariableNode;
        }

        private void before(InsnList instructions, Interceptor interceptor, AdviceConfig adviceConfig) {
            // if (joinPoint != null) {
            loadJoinPoint(instructions);
            LabelNode checkBeforeLabel = new LabelNode();
            AsmUtil.jump(instructions, Opcodes.IFNULL, checkBeforeLabel);
            if (adviceConfig.isThisEnabled() && !isStatic) {
                // joinPoint.setThat(this);
                loadJoinPoint(instructions);
                AsmUtil.loadThis(instructions);
                AsmUtil.invokeInterface(instructions, TYPE_JOINPOINT, METHOD_SETTHAT);
            }

            if (adviceConfig.isArgsEnabled()) {
                // joinPoint.setArgs(...);
                loadJoinPoint(instructions);
                AsmUtil.loadArgArray(instructions, methodNode);
                AsmUtil.invokeInterface(instructions, TYPE_JOINPOINT, METHOD_SETARGS);
            }

            if (adviceConfig.isArgEnabled()) {
                // joinPoint.setArg0(...);
                if (adviceConfig.isArg0Enabled()) {
                    loadJoinPoint(instructions);
                    AsmUtil.loadArg(isStatic, instructions, argumentTypes, 0);
                    AsmUtil.box(instructions, argumentTypes[0]);
                    AsmUtil.invokeInterface(instructions, TYPE_JOINPOINT, METHOD_SETARG0);
                }
                // joinPoint.setArg1(...);
                if (adviceConfig.isArg1Enabled()) {
                    loadJoinPoint(instructions);
                    AsmUtil.loadArg(isStatic, instructions, argumentTypes, 1);
                    AsmUtil.box(instructions, argumentTypes[1]);
                    AsmUtil.invokeInterface(instructions, TYPE_JOINPOINT, METHOD_SETARG1);
                }
                // joinPoint.setArg2(...);
                if (adviceConfig.isArg2Enabled()) {
                    loadJoinPoint(instructions);
                    AsmUtil.loadArg(isStatic, instructions, argumentTypes, 2);
                    AsmUtil.box(instructions, argumentTypes[2]);
                    AsmUtil.invokeInterface(instructions, TYPE_JOINPOINT, METHOD_SETARG2);
                }
            }

            if (interceptor.haveBeforeAdvice()) {
                // interceptor.before(joinPoint);
                loadInterceptor(instructions);
                loadJoinPoint(instructions);
                AsmUtil.invokeInterface(instructions, TYPE_INTERCEPTOR, METHOD_BEFORE);
            }
            // Label Enter..
            AsmUtil.labelNode(instructions, checkBeforeLabel);
        }

        private void after(InsnList instructions, Interceptor interceptor, int opcode) {
            // if (joinPoint != null) {
            loadJoinPoint(instructions);
            LabelNode checkAfterLabel = new LabelNode();
            AsmUtil.jump(instructions, Opcodes.IFNULL, checkAfterLabel);
            if (interceptor.getAdviceConfig().isReturnObjectEnabled()) {
                // joinPoint.setReturnObject(...);
                AsmUtil.loadReturnValue(instructions, opcode, returnType);
                loadJoinPoint(instructions);
                AsmUtil.swap(instructions);
                AsmUtil.invokeInterface(instructions, TYPE_JOINPOINT, METHOD_SETRETURNOBJECT);
            }
            // interceptor.after(joinPoint);
            loadInterceptor(instructions);
            loadJoinPoint(instructions);
            AsmUtil.invokeInterface(instructions, TYPE_INTERCEPTOR, METHOD_AFTER);

            // }
            AsmUtil.labelNode(instructions, checkAfterLabel);
        }

        private void afterThrowing(InsnList instructions, Interceptor interceptor) {
            LocalVariableNode throwVariableNode = initThrowableVariableNode();
            AsmUtil.storeLocal(instructions, TYPE_THROWABLE, throwVariableNode.index);
            if (interceptor.haveAfterThrowingAdvice()) {
                // if (joinPoint != null) {
                loadJoinPoint(instructions);
                LabelNode checkAfterThrowingLabel = new LabelNode();
                AsmUtil.jump(instructions, Opcodes.IFNULL, checkAfterThrowingLabel);
                if (interceptor.getAdviceConfig().isExceptionEnabled()) {
                    // joinPoint.setException(e);
                    loadJoinPoint(instructions);
                    loadThrowable(instructions);
                    AsmUtil.invokeInterface(instructions, TYPE_JOINPOINT, METHOD_SETEXCEPTION);
                }
                // interceptor.afterThrowing(joinPoint);
                loadInterceptor(instructions);
                loadJoinPoint(instructions);
                AsmUtil.invokeInterface(instructions, TYPE_INTERCEPTOR, METHOD_AFTERTHROWING);

                // }
                AsmUtil.labelNode(instructions, checkAfterThrowingLabel);
            }

            // throw e;
            loadThrowable(instructions);
            AsmUtil.athrow(instructions);
        }

        private void loadInterceptor(InsnList instructions) {
            AsmUtil.loadLocal(instructions, TYPE_INTERCEPTOR, interceptorVariableNode.index);
        }

        private void loadJoinPoint(InsnList instructions) {
            AsmUtil.loadLocal(instructions, TYPE_JOINPOINT, joinPointVariableNode.index);
        }

        private void loadThrowable(InsnList instructions) {
            AsmUtil.loadLocal(instructions, TYPE_THROWABLE, throwVariableNode.index);
        }

        private AbstractInsnNode findInitConstructorInstruction() {
            int nested = 0;
            for (AbstractInsnNode insnNode = this.methodNode.instructions.getFirst(); insnNode != null; insnNode = insnNode.getNext()) {
                if (insnNode instanceof TypeInsnNode) {
                    if (insnNode.getOpcode() == Opcodes.NEW) {
                        // new object().
                        nested++;
                    }
                } else if (insnNode instanceof MethodInsnNode) {
                    final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                    if (methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL && "<init>".equals(methodInsnNode.name)) {
                        if (--nested < 0) {
                            // find this() or super().
                            return insnNode.getNext();
                        }
                    }
                }
            }

            return null;
        }

        private LocalVariableNode addInterceptorLocalVariable(final String name, final Type type) {
            int index = this.nextLocals;
            this.nextLocals += type.getSize();
            methodNode.maxLocals = this.nextLocals;
            return new LocalVariableNode(name, type.getDescriptor(), null, interceptorVariableStartLabelNode, interceptorVariableEndLabelNode, index);
        }
    }

    class ASMTryCatch {
        private final MethodNode methodNode;
        private final LabelNode startLabelNode = new LabelNode();
        private final LabelNode endLabelNode = new LabelNode();

        public ASMTryCatch(final MethodNode methodNode) {
            this.methodNode = methodNode;

            final TryCatchBlockNode tryCatchBlockNode = new TryCatchBlockNode(this.startLabelNode, this.endLabelNode, this.endLabelNode, "java/lang/Throwable");
            if (this.methodNode.tryCatchBlocks == null) {
                this.methodNode.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
            }
            this.methodNode.tryCatchBlocks.add(tryCatchBlockNode);
        }

        public LabelNode getStartLabelNode() {
            return this.startLabelNode;
        }

        public LabelNode getEndLabelNode() {
            return this.endLabelNode;
        }

        public void sort() {
            if (this.methodNode.tryCatchBlocks == null) {
                return;
            }

            Collections.sort(this.methodNode.tryCatchBlocks, new Comparator<TryCatchBlockNode>() {
                public int compare(TryCatchBlockNode o1, TryCatchBlockNode o2) {
                    return blockLength(o1) - blockLength(o2);
                }

                private int blockLength(TryCatchBlockNode block) {
                    final int startidx = methodNode.instructions.indexOf(block.start);
                    final int endidx = methodNode.instructions.indexOf(block.end);
                    return endidx - startidx;
                }
            });

            // Updates the 'target' of each try catch block annotation.
            for (int i = 0; i < this.methodNode.tryCatchBlocks.size(); i++) {
                TryCatchBlockNode tryCatchBlock = (TryCatchBlockNode) methodNode.tryCatchBlocks.get(i);
                tryCatchBlock.updateIndex(i);
            }
        }
    }
}
