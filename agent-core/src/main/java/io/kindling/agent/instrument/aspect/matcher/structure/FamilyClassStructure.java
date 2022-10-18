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

package io.kindling.agent.instrument.aspect.matcher.structure;

import java.util.HashMap;
import java.util.Map;

import io.kindling.agent.api.ClassStructure;
import io.kindling.agent.util.LazyGet;

public abstract class FamilyClassStructure implements ClassStructure {
    private final LazyGet<Map<String, ClassStructure>> familyTypeClassStructuresLazyGet = new LazyGet<Map<String, ClassStructure>>() {
        protected Map<String, ClassStructure> initialValue() {
            final Map<String, ClassStructure> familyClassStructureMap = new HashMap<String, ClassStructure>();
            familyClassStructureMap.put(getJavaClassName(), getThisClassStructure());
            for (final ClassStructure interfaceClassStructure : getInterfaceClassStructures()) {
                familyClassStructureMap.put(interfaceClassStructure.getJavaClassName(), interfaceClassStructure);
                familyClassStructureMap.putAll(interfaceClassStructure.getFamilyTypeClassStructures());
            }

            final ClassStructure superClassStructure = getSuperClassStructure();
            if (null != superClassStructure) {
                familyClassStructureMap.put(superClassStructure.getJavaClassName(), superClassStructure);
                familyClassStructureMap.putAll(superClassStructure.getFamilyTypeClassStructures());
            }
            return familyClassStructureMap;
        }
    };

    private ClassStructure getThisClassStructure() {
        return this;
    }

    public Map<String, ClassStructure> getFamilyTypeClassStructures() {
        return familyTypeClassStructuresLazyGet.get();
    }

    public boolean isChild(String className) {
        Map<String, ClassStructure> map = getFamilyTypeClassStructures();
        if ("java.lang.Object".equals(className)) {
            return true;
        }
        return map.containsKey(className);
    }

    public int hashCode() {
        return getJavaClassName().hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof ClassStructure && getJavaClassName().equals(((ClassStructure) obj).getJavaClassName());
    }
}
