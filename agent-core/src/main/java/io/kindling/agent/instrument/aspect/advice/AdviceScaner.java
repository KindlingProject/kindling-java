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

package io.kindling.agent.instrument.aspect.advice;

import java.util.ArrayList;
import java.util.List;

import io.kindling.agent.api.MethodModifier;
import io.kindling.agent.deps.org.objectweb.asm.AnnotationVisitor;
import io.kindling.agent.deps.org.objectweb.asm.ClassVisitor;
import io.kindling.agent.deps.org.objectweb.asm.Opcodes;
import io.kindling.agent.deps.org.objectweb.asm.Type;
import io.kindling.agent.instrument.annotation.AdvicePointCut;
import io.kindling.agent.instrument.aspect.advice.info.AdviceContext;
import io.kindling.agent.instrument.aspect.advice.info.AnnotatedAdviceInfo;
import io.kindling.agent.instrument.aspect.pointcut.AnnotationPointCut;
import io.kindling.agent.service.ServiceFactory;
import io.kindling.agent.util.AsmBridge;

/**
 * Scan @AdvicePointCut
 */
public class AdviceScaner extends ClassVisitor {
    private static final String DESC_ADVICE_POINTCUT = Type.getDescriptor(AdvicePointCut.class);

    private String adviceName;
    private boolean isInterface;
    private AnnotatedAdviceInfo adviceInfo;
    private List<MethodModifier> matchMethodModifiers = new ArrayList<MethodModifier>();
    private List<String> matchClasses = new ArrayList<String>();
    private List<String> matchMethods = new ArrayList<String>();
    private List<String> matchParams = new ArrayList<String>();
    
    public AdviceScaner(ClassVisitor cv) {
        super(AsmBridge.ASM_API, cv);
    }
    
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.adviceName = name.replace('/', '.');
        this.isInterface = (access & Opcodes.ACC_INTERFACE) > 0;
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationVisitor av = super.visitAnnotation(desc, visible);
        if (isInterface == false) {
            if (DESC_ADVICE_POINTCUT.equals(desc)) {
                if (adviceInfo == null) {
                    adviceInfo = new AnnotatedAdviceInfo(adviceName);
                }
                av = new AnnotationVisitor(api, av) {
                    public AnnotationVisitor visitArray(String name) {
                        AnnotationVisitor av = super.visitArray(name);
                        if ("matchModifiers".equals(name)) {
                            av = new AnnotationVisitor(api, av) {
                                public void visitEnum(String name, String desc, String value) {
                                    super.visitEnum(name, desc, value);
                                    matchMethodModifiers.add(MethodModifier.of(value));
                                }
                            };
                        }
                        if ("matchClasses".equals(name)) {
                            av = new AnnotationVisitor(api, av) {
                                public void visit(String name, Object value) {
                                    super.visit(name, value);
                                    if (value != null) {
                                        matchClasses.add((String) value);
                                    }
                                }
                            };
                        } else if ("matchMethods".equals(name)) {
                            av = new AnnotationVisitor(api, av) {
                                public void visit(String name, Object value) {
                                    super.visit(name, value);
                                    if (value != null) {
                                        matchMethods.add((String) value);
                                    }
                                }
                            };
                        } else if ("matchParams".equals(name)) {
                            av = new AnnotationVisitor(api, av) {
                                public void visit(String name, Object value) {
                                    super.visit(name, value);
                                    if (value != null) {
                                        matchParams.add((String) value);
                                    }
                                }
                            };
                        }
                        return av;
                    }
                };
            }
        }
        return av;
    }

    public void visitEnd() {
        super.visitEnd();
        if (adviceInfo != null) {
            // Check Argument Count for matchClasses、matchMethods、matchParams.
            // 1:1:1
            // 1:1:N 1:N:1 N:1:1
            // 1:N:N N:1:N N:N:1
            // N:N:N
            if (matchClasses.size() > 1) {
                if (matchMethods.size() > 1 && matchClasses.size() != matchMethods.size())  {
                    ServiceFactory.LOG.info("[Ignore Advice] {} by bad argNums with [matchClasses: {}] and [matchMethods: {}]", adviceName, matchClasses.size(), matchMethods.size());
                    return;
                } else if (matchParams.size() > 1 && matchClasses.size() != matchParams.size())  {
                    ServiceFactory.LOG.info("[Ignore Advice] {} by bad argNums with [matchClasses] and [matchParams]", adviceName);
                    return;
                }
            } else if (matchMethods.size() > 1 && matchParams.size() > 1
                    && matchMethods.size() != matchParams.size())  {
                ServiceFactory.LOG.info("[Ignore Advice] {} by bad argNums with [matchMethods] and [matchParams]", adviceName);
                return;
            }
            int maxNum = matchClasses.size();
            if (matchMethods.size() > maxNum) {
                maxNum = matchMethods.size();
            }
            if (matchParams.size() > maxNum) {
                maxNum = matchParams.size();
            }
            if (matchMethodModifiers.isEmpty()) {
                for (int i = 0; i < maxNum; i++) {
                    matchMethodModifiers.add(MethodModifier.ALL);
                }
            } else if (matchMethodModifiers.size() == 1) {
                for (int i = 1; i < maxNum; i++) {
                    matchMethodModifiers.add(matchMethodModifiers.get(0));
                }
            } else if (matchMethodModifiers.size() != maxNum) {
                ServiceFactory.LOG.info("[Ignore Advice] {} by argNums with [matchMethodModifiers] must be {}.", adviceName, maxNum);
                return;
            }
            
            adviceInfo.setPointCut(new AnnotationPointCut(matchMethodModifiers.get(0), matchClasses.get(0), matchMethods.get(0), matchParams.get(0)));
            for (int i = 1; i < maxNum; i++) {
                MethodModifier matchMethodModifier = matchMethodModifiers.size() == 1 ? matchMethodModifiers.get(0) :matchMethodModifiers.get(i);
                String matchClass = matchClasses.size() == 1 ? matchClasses.get(0) : matchClasses.get(i);
                String matchMethod = matchMethods.size() == 1 ? matchMethods.get(0) : matchMethods.get(i);
                String matchParam = matchParams.size() == 1 ? matchParams.get(0) : matchParams.get(i);
                adviceInfo.getPointCut().addMatcher(matchMethodModifier, matchClass, matchMethod, matchParam);
            }
            AdviceContext.addAdviceInfo(adviceInfo);
        }
    }
}