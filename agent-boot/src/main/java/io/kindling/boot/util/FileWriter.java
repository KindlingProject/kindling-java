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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileWriter {
    private final OutputStream stream;
    private final DateFormat format;

    public FileWriter(String fileName) {
        this.stream = getFileOutputStream(fileName);
        this.format = new SimpleDateFormat("yyyy-MM:dd HH:mm:ss");
    }

    public void log(String action, String message) {
        try {
            stream.write(String.format("%s [%s] %s\n", format.format(new Date()), action, message).getBytes());
        } catch (IOException e) {}
    }
    
    public void error(String action, String message) {
        try {
            stream.write(String.format("%s [x %s] %s\n", format.format(new Date()), action, message).getBytes());
        } catch (IOException e) {}
    }
    
    public void flushAndclose() {
        if (stream != System.err) {
            try {
                stream.flush();
                stream.close();
            } catch (Exception e) {}
        }
    }

    private static OutputStream getFileOutputStream(String fileName) {
        if (fileName == null) {
            return System.err;
        }
        try {
            File file = new File(fileName);
            if (file.exists() == false) {
                file.createNewFile();
            }
            return new FileOutputStream(file, true);
        } catch (IOException e) {
        }
        return System.err;
    }
}
