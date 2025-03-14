/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument.scanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarFileScanner implements Scanner {

    private final JarFile jarFile;


    public static JarFileScanner of(String path) {
        Objects.requireNonNull(path, "path");
        return new JarFileScanner(new File(path));
    }

    public static JarFileScanner of(Path path) {
        Objects.requireNonNull(path, "path");
        return new JarFileScanner(path.toFile());
    }

    private JarFileScanner(File file) {
        Objects.requireNonNull(file, "file");
        try {
            this.jarFile = new JarFile(file);
        } catch (IOException e) {
            throw new IllegalStateException(file + " create fail");
        }
    }

    @Override
    public boolean exist(String fileName) {
        final JarEntry jarEntry = jarFile.getJarEntry(fileName);
        return jarEntry != null;
    }


    @Override
    public InputStream openStream(String fileName) {
        final JarEntry jarEntry = jarFile.getJarEntry(fileName);
        if (jarEntry == null) {
            return null;
        }
        try {
            return jarFile.getInputStream(jarEntry);
        } catch (IOException e) {
            return null;
        }
    }

    public void close() {
        try {
            jarFile.close();
        } catch (IOException ignored) {
            // ignore
        }
    }

    @Override
    public String toString() {
        return "JarFileScanner{" +
                "jarFile=" + jarFile.getName() +
                '}';
    }
}
