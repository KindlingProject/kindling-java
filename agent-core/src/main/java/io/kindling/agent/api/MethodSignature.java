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
import io.kindling.agent.deps.org.objectweb.asm.Type;

public class MethodSignature {
    protected String signature;
    protected int access;
    protected String modifier;
    protected String returnType;
    protected String ownerClassType;
    protected String methodName;
    /**
     * Args separate by comma.
     */
    protected String argTypes;

    private String[] argTypeArray;

    private boolean isStaticMethod;

    public MethodSignature() {
    }

    /**
     * The constructor of MethodSignature.
     * 
     * @param classType class name of method owner.
     * @param access modifier of method,1 is public,4 is protected,2 is private
     * @param methodName method name
     * @param desc method desc
     * @param exceptions exception names
     */
    public MethodSignature(String classType, int access, String methodName, String desc) {
        this.ownerClassType = classType;
        this.access = access;
        if ((access & Opcodes.ACC_PUBLIC) > 0) {
            modifier = "public";
        } else if ((access & Opcodes.ACC_PROTECTED) > 0) {
            modifier = "protected";
        } else if ((access & Opcodes.ACC_PRIVATE) > 0) {
            modifier = "private";
        }
        this.isStaticMethod = (access & Opcodes.ACC_STATIC) > 0;
        this.methodName = methodName;
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        if (argumentTypes != null && argumentTypes.length > 0) {
            argTypeArray = new String[argumentTypes.length];
            argTypes = "";
            for (int i = 0; i < argumentTypes.length; i++) {
                argTypeArray[i] = argumentTypes[i].getClassName();
                if (i > 0) {
                    argTypes += ",";
                }
                argTypes += argumentTypes[i].getClassName();
            }
        }
        returnType = Type.getReturnType(desc).getClassName();

        // build signature
        StringBuffer sb = new StringBuffer();
        if (modifier != null) {
            sb.append(modifier).append(" ");
        }
        sb.append(returnType).append(" ").append(ownerClassType).append(".").append(methodName).append("(");
        if (argTypes != null) {
            sb.append(argTypes);
        }
        sb.append(")");
        signature = sb.toString();
    }

    public String getSignature() {
        return signature;
    }

    public int getAccess() {
        return access;
    }

    public String getModifier() {
        return modifier;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getOwnerClassType() {
        return ownerClassType;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getArgTypes() {
        return argTypes;
    }

    public String[] getArgTypeArray() {
        return argTypeArray;
    }

    public boolean isStaticMethod() {
        return isStaticMethod;
    }

    public int getArgNum() {
        return argTypeArray == null ? 0 : argTypeArray.length;
    }

    public String toString() {
        return this.signature;
    }
}
