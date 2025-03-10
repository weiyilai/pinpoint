/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.classloading;

import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.Objects;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class PluginClassInjector implements ClassInjector {
    private final Logger logger = LogManager.getLogger(PluginClassInjector.class);

    private final ClassLoader bootstrapClassLoader = Object.class.getClassLoader();

    private final BootstrapCore bootstrapCore;
    private final ClassInjector bootstrapClassLoaderHandler;
    private final ClassInjector urlClassLoaderHandler;
    private final ClassInjector plainClassLoaderHandler;

    public static PluginClassInjector from(PluginConfig pluginConfig, InstrumentEngine instrumentEngine, BootstrapCore bootstrapCore) {
        Objects.requireNonNull(pluginConfig, "pluginConfig");

        ClassInjector bootstrapClassLoaderHandler = new BootstrapClassLoaderHandler(pluginConfig, instrumentEngine);
        ClassInjector urlClassLoaderHandler = new URLClassLoaderHandler(pluginConfig);

        final DefineClass defineClass = instrumentEngine.getDefineClass();
        ClassInjector plainClassLoaderHandler = new PlainClassLoaderHandler(defineClass, pluginConfig);
        return new PluginClassInjector(bootstrapCore, bootstrapClassLoaderHandler, urlClassLoaderHandler, plainClassLoaderHandler);
    }

    public PluginClassInjector(BootstrapCore bootstrapCore,
                               ClassInjector bootstrapClassInjector,
                               ClassInjector urlClassLoaderInjector,
                               ClassInjector plainClassLoaderInjector) {
        this.bootstrapCore = Objects.requireNonNull(bootstrapCore, "bootstrapCore");
        this.bootstrapClassLoaderHandler = Objects.requireNonNull(bootstrapClassInjector, "bootstrapClassInjector");
        this.urlClassLoaderHandler = Objects.requireNonNull(urlClassLoaderInjector, "urlClassLoaderInjector");
        this.plainClassLoaderHandler = Objects.requireNonNull(plainClassLoaderInjector, "plainClassLoaderInjector");
    }

    @Override
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        try {
            if (bootstrapCore.isBootstrapPackage(className)) {
                return bootstrapCore.loadClass(className);
            }

            if (bootstrapClassLoader == classLoader) {
                return bootstrapClassLoaderHandler.injectClass(null, className);
            } else if (classLoader instanceof URLClassLoader) {
                return urlClassLoaderHandler.injectClass(classLoader, className);
            } else {
                return plainClassLoaderHandler.injectClass(classLoader, className);
            }
        } catch (Throwable e) {
            // fixed for LinkageError
            logger.warn("Failed to load plugin class {} with classLoader {}", className, classLoader, e);
            throw new PinpointException("Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String internalName) {
        try {
            if (bootstrapCore.isBootstrapPackageByInternalName(internalName)) {
                return bootstrapCore.openStream(internalName);
            }
            if (targetClassLoader == null) {
                return bootstrapClassLoaderHandler.getResourceAsStream(null, internalName);
            } else if (targetClassLoader instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) targetClassLoader;
                return urlClassLoaderHandler.getResourceAsStream(urlClassLoader, internalName);
            } else {
                return plainClassLoaderHandler.getResourceAsStream(targetClassLoader, internalName);
            }
        } catch (Throwable e) {
             logger.warn("Failed to load plugin resource as stream {} with classLoader {}", internalName, targetClassLoader, e);
            return null;
        }
    }
}