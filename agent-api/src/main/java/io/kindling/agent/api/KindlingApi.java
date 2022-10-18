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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class KindlingApi {
    private static FileOutputStream out;

    static {
        String fileName = System.getProperty("kindling_out", "/dev/null");
        try {
            File devNull = new File(fileName);
            if (devNull.exists()) {
                out = new FileOutputStream(devNull);
            }
        } catch (IOException e) {
        }
        if (out == null) {
            System.err.println("[File Not Exist] " + fileName);
        }
    }

    public static void enter(String traceId) {
        if (out == null) {
            return;
        }
        recordTraceId(traceId, true);
    }

    public static void exit(String traceId) {
        if (out == null) {
            return;
        }
        recordTraceId(traceId, false);
    }

    private static void recordTraceId(String traceId, boolean enter) {
        synchronized (out) {
            try {
                out.write(String.format("kd-txid@%s!%d!", traceId, enter ? 1 : 0).getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
