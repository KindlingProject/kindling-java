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

package io.kindling.boot;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import io.kindling.boot.exception.AttachException;
import io.kindling.boot.util.FileWriter;
import io.kindling.boot.util.Version;

public class KindlingBootstrap {
    private static File agentJar;
    private static String version;
    private static volatile boolean started;

    /**
     * Attach Kindling Agent
     * 
     * @param agentArgs start/stop,file=xxx,log=xxx,version=xxx
     * @param inst
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        Map<String, String> featureMap = toFeatureMap(agentArgs);
        FileWriter writer = new FileWriter(featureMap.get("log"));
        try {
            if (featureMap.containsKey("start")) {
                if (started) {
                    throw new AttachException("Attach", "Aleady attached, no need to attach one more time.");
                }

                String versionVal = featureMap.get("version");
                if (versionVal == null) {
                    throw new AttachException("Attach", "version is not set.");
                }
                /**
                 * Can't attach with different version.
                 * 
                 * √ attach v1/detach v1/attach v1
                 * × attach v1/detach v1/attach v2
                 */
                if (agentJar == null) {
                    try {
                        // Append Once.
                        version = versionVal;
                        agentJar = getAgentJarFile(versionVal);
                        appendAgentCoreJarToSearch(inst, agentJar);
                    } catch (Exception cause) {
                        cause.printStackTrace();
                        throw new AttachException("Attach", cause.getMessage());
                    }
                }
                install(writer, inst, featureMap);
                started = true;
            } else {
                if (started == false) {
                    throw new AttachException("Detach", "No need to detach, application is not attached.");
                }
                install(writer, inst, featureMap);
                started = false;
            }
        } catch (AttachException cause) {
            writer.error(cause.getAction(), cause.getMessage());
        }
        writer.flushAndclose();
    }

    /**
     * JavaAgent Kindling Agent
     * 
     * @param agentArgs <empty>
     * @param inst
     */
    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        String javaVersion = System.getProperty("java.version", "");
        if (javaVersion.startsWith("1.5")) {
            System.err.println("Java version is: " + javaVersion + ". APM agent only support Java 1.6+.");
            return;
        }

        // Read from Version File.
        File jarFolder = new File(KindlingBootstrap.class.getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart()).getParentFile();
        String agentVersion = Version.getVersionFromFile(jarFolder);
        if (agentVersion == null) {
            return;
        }
        File agentJar = getAgentJarFile(agentVersion);
        appendAgentCoreJarToSearch(inst, agentJar);

        Class<?> agentClass = ClassLoader.getSystemClassLoader().loadClass("io.kindling.agent.KindlingAgent");
        Method premain = agentClass.getDeclaredMethod("premain", Map.class, Instrumentation.class, File.class);
        premain.invoke(null, new HashMap<String, String>(), inst, agentJar);
    }
    
    private static File getAgentJarFile(String version) throws Exception {
        File jarFolder = new File(KindlingBootstrap.class.getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart()).getParentFile();
        return new File(jarFolder, version + "/agent-core.jar");
    }

    private static void appendAgentCoreJarToSearch(Instrumentation inst, File agentJarFile) throws Exception {
        JarFile agentCoreJar = new JarFile(agentJarFile);
        System.out.println("Found Jar: " + agentCoreJar.getName());
        inst.appendToBootstrapClassLoaderSearch(agentCoreJar);
        inst.appendToSystemClassLoaderSearch(agentCoreJar);
    }

    private static void install(FileWriter writer, Instrumentation inst, Map<String, String> featureMap) throws AttachException {
        String mode = featureMap.containsKey("start") ? "Attach" : "Detach";
        writer.log("Start " + mode, version);
        // Load Agent in agent-core.jar.
        try {
            Class<?> agentClass = ClassLoader.getSystemClassLoader().loadClass("io.kindling.agent.KindlingAgent");
            Method premain = agentClass.getDeclaredMethod("premain", Map.class, Instrumentation.class, File.class);
            premain.invoke(null, featureMap, inst, agentJar);
        } catch (Exception cause) {
            cause.printStackTrace();
            throw new AttachException(mode, cause.getMessage());
        }
        writer.log("Finish " + mode, version);
    }

    private static Map<String, String> toFeatureMap(String featureString) {
        final Map<String, String> featureMap = new HashMap<String, String>();
        if (isBlankString(featureString)) {
            return featureMap;
        }

        // KeyValues split by ,
        final String[] kvPairSegmentArray = featureString.split("\\,");
        if (kvPairSegmentArray.length <= 0) {
            return featureMap;
        }

        for (String kvPairSegmentString : kvPairSegmentArray) {
            // KeyValue split by =.
            final String[] kvSegmentArray = kvPairSegmentString.split("=");
            if (kvSegmentArray.length == 1) {
                putValue(featureMap, kvSegmentArray[0], "");
            } else if (kvSegmentArray.length == 2 && !isBlankString(kvSegmentArray[0]) && !isBlankString(kvSegmentArray[1])) {
                putValue(featureMap, kvSegmentArray[0], kvSegmentArray[1]);
            }
        }
        return featureMap;
    }

    private static void putValue(Map<String, String> map, String key, String value) {
        if (map.containsKey(key)) {
            if (value != null && value.length() > 0) {
                map.put(key, map.get(key) + "," + value);
            }
        } else {
            map.put(key, value);
        }
    }

    private static boolean isBlankString(final String str) {
        return str == null || str.trim().length() == 0;
    }
}
