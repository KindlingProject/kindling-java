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

package io.kindling.agent.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LogService implements ILogService {
    private final OutputStream stream;

    public LogService(String fileName) {
        this.stream = getFileOutputStream(fileName);
    }

    public void error(String message) {
        log("ERROR " + message + "\n");
    }

    public void error(String message, Throwable cause) {
        log("ERROR " + message + ", Cause: " + cause.getMessage() + "\n");
        cause.printStackTrace();
    }

    public void info(Object message) {
        log("INFO " + message + "\n");
    }

    public void info(String format, Object... argArray) {
        if (argArray[argArray.length - 1] instanceof Throwable) {
            Throwable throwable = (Throwable) argArray[argArray.length - 1];
            if (argArray.length == 1) {
                log("INFO " + format + ", Cause: " + throwable.getMessage() + "\n");
            } else {
                log("INFO " + formatMessage(format, true, argArray) + ", Cause: " + throwable.getMessage() + "\n");
            }
        } else {
            log("INFO " + formatMessage(format, false, argArray) + "\n");
        }
    }

    private static String formatMessage(String format, boolean isLastThrowable, Object... argArray) {
        int delimIndex = format.indexOf("{}");
        if (delimIndex == -1) {
            return format;
        }

        StringBuffer message = new StringBuffer(format.length() + 50);
        int argLength = isLastThrowable ? argArray.length - 1 : argArray.length;
        int argIndex = 0, startIndex = 0;
        while (delimIndex != -1 && argIndex < argLength) {
            message.append(format.substring(startIndex, delimIndex));
            message.append(argArray[argIndex++]);
            startIndex = delimIndex + 2;
            delimIndex = format.indexOf("{}", startIndex);
        }
        message.append(format.substring(startIndex, format.length()));
        return message.toString();
    }

    private void log(String message) {
        try {
            stream.write(message.getBytes());
        } catch (IOException e) {
        }
    }

    private static OutputStream getFileOutputStream(String fileName) {
        if (fileName == null) {
            return System.out;
        }
        try {
            File file = new File(fileName);
            if (file.exists() == false) {
                file.createNewFile();
            }
            return new FileOutputStream(file, true);
        } catch (IOException e) {
        }
        return System.out;
    }
}
