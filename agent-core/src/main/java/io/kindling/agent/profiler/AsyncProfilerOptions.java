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

package io.kindling.agent.profiler;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public enum AsyncProfilerOptions {
    Interval("kindling_interval", "interval", "10000000"),
    Depth("kindling_depth", "jstackdepth", "20"),
    LogFile("kindling_log", "log", "/tmp/kindling/agent.log"),
    OutFile("kindling_out", "out", "/dev/null");

    private final String property;
    private final String key;
    private final String defaultValue;

    private AsyncProfilerOptions(String property, String key, String defaultValue) {
        this.property = property;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    private String getOptionValue(Map<String, String> featureMap) {
        String value = null;
        if (featureMap.isEmpty()) {
            value = System.getProperty(property, defaultValue);
        } else {
            value = featureMap.get(key);
        }
        if (value == null) {
            return defaultValue;
        }
        return value == null ? defaultValue : value;
    }

    public static long getIntervalMs(Map<String, String> featureMap) {
        String intervalNs = Interval.getOptionValue(featureMap);
        if (intervalNs.length() < 7 || intervalNs.length() > 9) {
            intervalNs = Interval.defaultValue;
        }
        return Long.valueOf(intervalNs) / 1000000L;
    }

    public static int getDepth(Map<String, String> featureMap) {
        String depth = Depth.getOptionValue(featureMap);
        if (depth.length() < 1 || depth.length() > 4) {
            depth = Depth.defaultValue;
        }
        return Integer.valueOf(depth);
    }

    public static String getLogFile(Map<String, String> featureMap) {
        return LogFile.getOptionValue(featureMap);
    }

    public static String getDefaultLogFile() {
        return LogFile.defaultValue;
    }

    public static void prepareOutFile(Map<String, String> featureMap) throws IOException {
        String outFile = OutFile.getOptionValue(featureMap);

        if (OutFile.defaultValue.equals(outFile) == false) {
            File out = new File(outFile);
            if (out.exists() == false) {
                out.createNewFile();
            }
        }
        System.setProperty("kindling_out", outFile);
    }
}
