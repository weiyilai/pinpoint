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

import com.navercorp.pinpoint.bootstrap.classloader.PinpointClassLoaderFactory;
import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import com.navercorp.pinpoint.profiler.plugin.JarPlugin;
import com.navercorp.pinpoint.profiler.plugin.Plugin;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import com.navercorp.pinpoint.profiler.plugin.PluginJar;
import com.navercorp.pinpoint.profiler.plugin.PluginPackageClassRequirementFilter;
import com.navercorp.pinpoint.profiler.plugin.PluginPackageFilter;
import org.apache.commons.lang3.CharUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginClassInjectorTest {

    public static final String CONTEXT_TYPE_MATCH_CLASS_LOADER = "org.springframework.context.support.ContextTypeMatchClassLoader";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final List<String> COMMONS_LANG = Collections.singletonList(CharUtils.class.getPackage().getName());

    private final DefineClass defineClass = DefineClassFactory.getDefineClass();

    @Test
    public void testInjectClass() throws Exception {
        String className = CharUtils.class.getName();
        final Plugin<?> plugin = getMockPlugin(className);

        final ClassLoader contextTypeMatchClassLoader = createContextTypeMatchClassLoader(new URL[]{plugin.getURL()});

        final PluginPackageFilter pluginPackageFilter = new PluginPackageFilter(COMMONS_LANG);
        final PluginPackageClassRequirementFilter pluginPackageRequirementFilter = new PluginPackageClassRequirementFilter(Collections.emptyList());
        PluginConfig pluginConfig = new PluginConfig(plugin, pluginPackageFilter, pluginPackageRequirementFilter);
        logger.debug("pluginConfig:{}", pluginConfig);

        ClassInjector injector = new PlainClassLoaderHandler(defineClass, pluginConfig);
        final Class<?> commonsLangClass = injector.injectClass(contextTypeMatchClassLoader, className);

        logger.debug("ClassLoader:{}", commonsLangClass.getClassLoader());
        Assertions.assertEquals(commonsLangClass.getName(), className, "check className");
        Assertions.assertEquals(commonsLangClass.getClassLoader().getClass().getName(), CONTEXT_TYPE_MATCH_CLASS_LOADER, "check ClassLoader");

    }

    private ClassLoader createContextTypeMatchClassLoader(URL[] urlArray) {
        try {
            final ClassLoader classLoader = this.getClass().getClassLoader();
            final Class<ClassLoader> aClass = (Class<ClassLoader>) classLoader.loadClass(CONTEXT_TYPE_MATCH_CLASS_LOADER);
            final Constructor<ClassLoader> constructor = aClass.getConstructor(ClassLoader.class);
            constructor.setAccessible(true);

            List<String> lib = COMMONS_LANG;
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            String className = this.getClass().getName();
            ClassLoader testClassLoader = PinpointClassLoaderFactory.createClassLoader(className, urlArray, systemClassLoader, lib);

            final ClassLoader contextTypeMatchClassLoader = constructor.newInstance(testClassLoader);

            logger.debug("cl:{}", contextTypeMatchClassLoader);

//        final Method excludePackage = aClass.getMethod("excludePackage", String.class);
//        ReflectionUtils.invokeMethod(excludePackage, contextTypeMatchClassLoader, "org.slf4j");


            return contextTypeMatchClassLoader;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }


    private Plugin<?> getMockPlugin(String className) throws Exception {
        ClassLoader cl = ClassLoaderUtils.getDefaultClassLoader();
        Class<?> clazz = null;
        try {
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(className + " class not found. Caused by:" + ex.getMessage(), ex);
        }
        return getMockPlugin(clazz);
    }

    private Plugin<?> getMockPlugin(Class<?> clazz) throws Exception {

        final URL location = CodeSourceUtils.getCodeLocation(clazz);
        URI uri = location.toURI();
        logger.debug("url:{}", location);
        PluginJar pluginJar = PluginJar.fromFilePath(Paths.get(uri));
        return new JarPlugin<>(pluginJar, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Test
    public void testInjectClass_bootstrap() {
        BootstrapCore bootstrapCore = new BootstrapCore(Collections.emptyList());
        ClassInjector boot = mock(ClassInjector.class);
        ClassInjector url = mock(ClassInjector.class);
        ClassInjector plain = mock(ClassInjector.class);

        ClassInjector injector = new PluginClassInjector(bootstrapCore, boot, url, plain);
        ClassLoader booptstrapCl = Object.class.getClassLoader();
        injector.injectClass(booptstrapCl, "java.lang.String");
        injector.injectClass(this.getClass().getClassLoader(), "Test");

        verify(boot).injectClass(booptstrapCl, "java.lang.String");
        verify(boot, never()).injectClass(this.getClass().getClassLoader(), "Test");
    }

}