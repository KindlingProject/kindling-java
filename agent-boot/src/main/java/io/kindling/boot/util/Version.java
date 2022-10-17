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

package io.kindling.boot.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Version {
    public static String getVersionFromFile(File folder) {
        File versionFile = new File(folder, "version");
        if (versionFile.exists() == false) {
            System.err.println("File version is not exist.");
            return null;
        }
        
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(versionFile));
            return br.readLine();
        } catch (IOException cause) {
            cause.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch(IOException e) {}
            }
        }
    }
}
