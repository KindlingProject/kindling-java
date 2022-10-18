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

import io.kindling.agent.deps.org.objectweb.asm.Opcodes;
import io.kindling.agent.deps.org.objectweb.asm.Type;
import io.kindling.agent.deps.org.objectweb.asm.commons.Method;
import io.kindling.agent.deps.org.objectweb.asm.tree.InsnList;
import io.kindling.agent.deps.org.objectweb.asm.tree.InsnNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.IntInsnNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.JumpInsnNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.LabelNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.LdcInsnNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.MethodInsnNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.MethodNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.TypeInsnNode;
import io.kindling.agent.deps.org.objectweb.asm.tree.VarInsnNode;

public class AsmUtil {
    private static final Type BYTE_TYPE = Type.getObjectType("java/lang/Byte");
    private static final Type BOOLEAN_TYPE = Type.getObjectType("java/lang/Boolean");
    private static final Type SHORT_TYPE = Type.getObjectType("java/lang/Short");
    private static final Type CHARACTER_TYPE = Type.getObjectType("java/lang/Character");
    private static final Type INTEGER_TYPE = Type.getObjectType("java/lang/Integer");
    private static final Type FLOAT_TYPE = Type.getObjectType("java/lang/Float");
    private static final Type LONG_TYPE = Type.getObjectType("java/lang/Long");
    private static final Type DOUBLE_TYPE = Type.getObjectType("java/lang/Double");
    private static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");

    private static final Type NUMBER_TYPE = Type.getObjectType("java/lang/Number");
    private static final Method BOOLEAN_VALUE = Method.getMethod("boolean booleanValue()");
    private static final Method CHAR_VALUE = Method.getMethod("char charValue()");
    private static final Method INT_VALUE = Method.getMethod("int intValue()");
    private static final Method FLOAT_VALUE = Method.getMethod("float floatValue()");
    private static final Method LONG_VALUE = Method.getMethod("long longValue()");
    private static final Method DOUBLE_VALUE = Method.getMethod("double doubleValue()");

    public static boolean isConstructor(MethodNode methodNode) {
        return methodNode.name != null && "<init>".equals(methodNode.name);
    }

    public static boolean isStatic(MethodNode methodNode) {
        return (methodNode.access & Opcodes.ACC_STATIC) != 0;
    }

    public static void loadThis(final InsnList instructions) {
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    }

    public static void loadArgArray(final InsnList instructions, MethodNode methodNode) {
        boolean isStatic = isStatic(methodNode);
        Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
        push(instructions, argumentTypes.length);
        newArray(instructions, OBJECT_TYPE);
        for (int i = 0; i < argumentTypes.length; i++) {
            dup(instructions);
            push(instructions, i);
            loadArg(isStatic, instructions, argumentTypes, i);
            box(instructions, argumentTypes[i]);
            arrayStore(instructions, OBJECT_TYPE);
        }
    }

    public static void loadArg(boolean staticAccess, final InsnList instructions, Type[] argumentTypes, int i) {
        final int index = getArgIndex(staticAccess, argumentTypes, i);
        final Type type = argumentTypes[i];
        instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), index));
    }

    public static void loadReturnValue(final InsnList instructions, int opcode, Type returnType) {
        if (opcode == Opcodes.RETURN) {
            loadNull(instructions);
        } else if (opcode == Opcodes.ARETURN) {
            dup(instructions);
        } else {
            if (opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {
                dup2(instructions);
            } else {
                dup(instructions);
            }
            box(instructions, returnType);
        }
    }

    static int getArgIndex(boolean staticAccess, final Type[] argumentTypes, final int arg) {
        int index = staticAccess ? 0 : 1;
        for (int i = 0; i < arg; i++) {
            index += argumentTypes[i].getSize();
        }
        return index;
    }

    static void box(final InsnList instructions, Type type) {
        if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
            return;
        }

        if (type == Type.VOID_TYPE) {
            // push null
            loadNull(instructions);
        } else {
            Type boxed = getBoxedType(type);
            // new instance.
            newInstance(instructions, boxed);
            if (type.getSize() == 2) {
                // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
                // dupX2
                dupX2(instructions);
                // dupX2
                dupX2(instructions);
                // pop
                pop(instructions);
            } else {
                // p -> po -> opo -> oop -> o
                // dupX1
                dupX1(instructions);
                // swap
                swap(instructions);
            }
            invokeConstructor(instructions, boxed, new Method("<init>", Type.VOID_TYPE, new Type[]{type}));
        }
    }

    private static void invokeConstructor(final InsnList instructions, final Type type, final Method method) {
        String owner = type.getSort() == Type.ARRAY ? type.getDescriptor() : type.getInternalName();
        instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, owner, method.getName(), method.getDescriptor(), false));
    }

    private static Type getBoxedType(final Type type) {
        switch (type.getSort()) {
            case Type.BYTE :
                return BYTE_TYPE;
            case Type.BOOLEAN :
                return BOOLEAN_TYPE;
            case Type.SHORT :
                return SHORT_TYPE;
            case Type.CHAR :
                return CHARACTER_TYPE;
            case Type.INT :
                return INTEGER_TYPE;
            case Type.FLOAT :
                return FLOAT_TYPE;
            case Type.LONG :
                return LONG_TYPE;
            case Type.DOUBLE :
                return DOUBLE_TYPE;
        }
        return type;
    }

    private static void newInstance(final InsnList instructions, final Type type) {
        instructions.add(new TypeInsnNode(Opcodes.NEW, type.getInternalName()));
    }

    static void push(InsnList insnList, final int value) {
        if (value >= -1 && value <= 5) {
            insnList.add(new InsnNode(Opcodes.ICONST_0 + value));
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            insnList.add(new IntInsnNode(Opcodes.BIPUSH, value));
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            insnList.add(new IntInsnNode(Opcodes.SIPUSH, value));
        } else {
            insnList.add(new LdcInsnNode(value));
        }
    }

    private static void pop(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.POP));
    }

    static void swap(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.SWAP));
    }
    private static void loadNull(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.ACONST_NULL));
    }

    private static void newArray(final InsnList insnList, final Type type) {
        insnList.add(new TypeInsnNode(Opcodes.ANEWARRAY, type.getInternalName()));
    }

    private static void dup(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.DUP));
    }

    private static void dup2(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.DUP2));
    }

    private static void dupX1(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.DUP_X1));
    }

    private static void dupX2(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.DUP_X2));
    }

    private static void arrayStore(final InsnList instructions, final Type type) {
        instructions.add(new InsnNode(type.getOpcode(Opcodes.IASTORE)));
    }

    static void invokeStatic(final InsnList instructions, final Type owner, final Method method) {
        invokeInsn(instructions, Opcodes.INVOKESTATIC, owner, method, false);
    }

    static void invokeStatic(final InsnList instructions, final String owner, final String methodName, String methodDesc) {
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, methodName, methodDesc, false));
    }

    static void invokeInterface(final InsnList instructions, final Type owner, final Method method) {
        invokeInsn(instructions, Opcodes.INVOKEINTERFACE, owner, method, true);
    }

    private static void invokeInsn(final InsnList instructions, final int opcode, final Type type, final Method method, final boolean isInterface) {
        String owner = type.getSort() == Type.ARRAY ? type.getDescriptor() : type.getInternalName();
        instructions.add(new MethodInsnNode(opcode, owner, method.getName(), method.getDescriptor(), isInterface));
    }

    static void storeLocal(final InsnList instructions, Type type, final int index) {
        instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ISTORE), index));
    }

    static void loadLocal(final InsnList instructions, Type type, final int index) {
        instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), index));
    }

    static void jump(final InsnList instructions, final int opcode, final LabelNode node) {
        instructions.add(new JumpInsnNode(opcode, node));
    }

    static void labelNode(final InsnList instructions, final LabelNode node) {
        instructions.add(node);
    }

    static void checkCast(final InsnList instructions, final Type type) {
        if (!type.equals(OBJECT_TYPE)) {
            instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, type.getInternalName()));
        }
    }

    static boolean isReturnCode(int opcode) {
        switch (opcode) {
            case Opcodes.RETURN : // empty stack
            case Opcodes.IRETURN : // 1 before n/a after
            case Opcodes.FRETURN : // 1 before n/a after
            case Opcodes.ARETURN : // 1 before n/a after
            case Opcodes.LRETURN : // 2 before n/a after
            case Opcodes.DRETURN : // 2 before n/a after
                return true;
            default :
                return false;
        }
    }

    static void checkCastReturn(final InsnList instructions, final Type type) {
        final int sort = type.getSort();
        switch (sort) {
            case Type.VOID : {
                pop(instructions);
                instructions.add(new InsnNode(Opcodes.RETURN));
                break;
            }
            case Type.BOOLEAN :
            case Type.CHAR :
            case Type.BYTE :
            case Type.SHORT :
            case Type.INT : {
                unbox(instructions, type);
                instructions.add(new InsnNode(type.getOpcode(Opcodes.IRETURN)));
                break;
            }
            case Type.FLOAT : {
                unbox(instructions, type);
                instructions.add(new InsnNode(Opcodes.FRETURN));
                break;
            }
            case Type.LONG : {
                unbox(instructions, type);
                instructions.add(new InsnNode(Opcodes.LRETURN));
                break;
            }
            case Type.DOUBLE : {
                unbox(instructions, type);
                instructions.add(new InsnNode(Opcodes.DRETURN));
                break;
            }
            case Type.ARRAY :
            case Type.OBJECT :
            case Type.METHOD :
            default : {
                // checkCast(returnType);
                unbox(instructions, type);
                instructions.add(new InsnNode(Opcodes.ARETURN));
                break;
            }
        }
    }

    private static void unbox(final InsnList instructions, final Type type) {
        Type t = NUMBER_TYPE;
        Method sig = null;
        switch (type.getSort()) {
            case Type.VOID :
                return;
            case Type.CHAR :
                t = CHARACTER_TYPE;
                sig = CHAR_VALUE;
                break;
            case Type.BOOLEAN :
                t = BOOLEAN_TYPE;
                sig = BOOLEAN_VALUE;
                break;
            case Type.DOUBLE :
                sig = DOUBLE_VALUE;
                break;
            case Type.FLOAT :
                sig = FLOAT_VALUE;
                break;
            case Type.LONG :
                sig = LONG_VALUE;
                break;
            case Type.INT :
            case Type.SHORT :
            case Type.BYTE :
                sig = INT_VALUE;
        }
        if (sig == null) {
            instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, type.getInternalName()));
        } else {
            instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, t.getInternalName()));
            instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, t.getInternalName(), sig.getName(), sig.getDescriptor(), false));
        }
    }

    static void athrow(final InsnList instructions) {
        instructions.add(new InsnNode(Opcodes.ATHROW));
    }
}
