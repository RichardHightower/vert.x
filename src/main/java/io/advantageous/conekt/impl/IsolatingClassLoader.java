/*
 *
 *  * Copyright (c) 2011-2016 The original author or authors
 *  * This project contains modified work from the Vert.x Project.
 *  * The Vert.x project Copyright is owned by Red Hat and/or the
 *  * original authors of the Vert.x project including Tim Fox, Julien Vet,
 *  * Norman Maurer, and many others.
 *  * We have left the original author tags on this MODIFIED COPY/FORK.
 *  *
 *  * Modified work is Copyright (c) 2015-2016 Rick Hightower and Geoff Chandler.
 *  * ------------------------------------------------------
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the Eclipse Public License v1.0
 *  * and Apache License v2.0 which accompanies this distribution.
 *  *
 *  *     The Eclipse Public License is available at
 *  *     http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *     The Apache License v2.0 is available at
 *  *     http://www.opensource.org/licenses/apache2.0.php
 *  *
 *  * You may elect to redistribute this code under either of these licenses.
 *
 */

package io.advantageous.conekt.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class IsolatingClassLoader extends URLClassLoader {

    private List<String> isolatedClasses;

    public IsolatingClassLoader(URL[] urls, ClassLoader parent, List<String> isolatedClasses) {
        super(urls, parent);
        this.isolatedClasses = isolatedClasses;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                if (isIsolatedClass(name)) {
                    // We don't want to load Vert.x (or Vert.x dependency) classes from an isolating loader
                    if (isVertxOrSystemClass(name)) {
                        try {
                            c = getParent().loadClass(name);
                        } catch (ClassNotFoundException e) {
                            // Fall through
                        }
                    }
                    if (c == null) {
                        // Try and load with this classloader
                        try {
                            c = findClass(name);
                        } catch (ClassNotFoundException e) {
                            // Now try with parent
                            c = getParent().loadClass(name);
                        }
                    }
                    if (resolve) {
                        resolveClass(c);
                    }
                } else {
                    // Parent first
                    c = super.loadClass(name, resolve);
                }
            }
            return c;
        }
    }

    private boolean isIsolatedClass(String name) {
        if (isolatedClasses != null) {
            for (String isolated : isolatedClasses) {
                if (isolated.endsWith(".*")) {
                    String isolatedPackage = isolated.substring(0, isolated.length() - 1);
                    String paramPackage = name.substring(0, name.lastIndexOf('.') + 1);
                    if (paramPackage.startsWith(isolatedPackage)) {
                        // Matching package
                        return true;
                    }
                } else if (isolated.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public URL getResource(String name) {

        // First check this classloader
        URL url = findResource(name);

        // Then try the parent if not found
        if (url == null) {
            url = super.getResource(name);
        }

        return url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {

        // First get resources from this classloader
        List<URL> resources = Collections.list(findResources(name));

        // Then add resources from the parent
        if (getParent() != null) {
            Enumeration<URL> parentResources = getParent().getResources(name);
            if (parentResources.hasMoreElements()) {
                resources.addAll(Collections.list(parentResources));
            }
        }

        return Collections.enumeration(resources);
    }

    private boolean isVertxOrSystemClass(String name) {
        return
                name.startsWith("java.") ||
                        name.startsWith("javax.") ||
                        name.startsWith("sun.*") ||
                        name.startsWith("com.sun.") ||
                        name.startsWith("io.conekt.core") ||
                        name.startsWith("io.netty.") ||
                        name.startsWith("com.fasterxml.jackson");
    }
}
