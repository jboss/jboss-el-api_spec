/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.el.cache;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Stuart Douglas
 */
public class FactoryFinderCache {

    private static final Map<CacheKey, String> CLASS_CACHE = new ConcurrentHashMap<CacheKey, String>();

    /**
     * Called by the container at deployment time to set the name of a given factory, to remove the need for the
     * implementation to look it up on every call.
     *
     * @param classLoader      The deployments class loader
     * @param factoryId        The type of factory that is being recorded (at this stage only javax.el.ExpressionFactory has any effect
     * @param factoryClassName The name of the factory class that is present in the deployment, or null if none is present
     */
    public static void addCacheEntry(final ClassLoader classLoader, final String factoryId, final String factoryClassName) {
        if (factoryClassName == null) {
            CLASS_CACHE.put(new CacheKey(classLoader, factoryId), "");
        } else {
            CLASS_CACHE.put(new CacheKey(classLoader, factoryId), factoryClassName);
        }
    }

    /**
     * This should be called by the container on undeploy to remove all references to the given class loader
     * from the cache.
     *
     * @param classLoader The class loader to remove
     */
    public static void clearClassLoader(final ClassLoader classLoader) {
        BeanPropertiesCache.clear(classLoader);
        final Iterator<Map.Entry<CacheKey, String>> it = CLASS_CACHE.entrySet().iterator();
        while (it.hasNext()) {
            final CacheKey key = it.next().getKey();
            if (key.loader == classLoader) {
                it.remove();
            }
        }
    }

    public static String loadImplementationClassName(final String factoryId, final ClassLoader classLoader) {

        final Map<CacheKey, String> classCache = CLASS_CACHE;
        if (classCache != null) {
            final String value = classCache.get(new CacheKey(classLoader, factoryId));
            if (value != null) {
                if (value.equals("")) {
                    return null;
                }
                return value;
            }
        }

        String serviceId = "META-INF/services/" + factoryId;
        // try to find services in CLASSPATH
        try {
            InputStream is = null;
            if (classLoader == null) {
                is = ClassLoader.getSystemResourceAsStream(serviceId);
            } else {
                is = classLoader.getResourceAsStream(serviceId);
            }

            if (is != null) {
                BufferedReader rd =
                        new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String factoryClassName = rd.readLine();
                rd.close();

                if (factoryClassName != null &&
                        !"".equals(factoryClassName)) {
                    if (classCache != null) {
                        classCache.put(new CacheKey(classLoader, factoryId), factoryClassName);
                    }
                    return factoryClassName;
                }
            }
        } catch (Exception ex) {
        }
        if (classCache != null) {
            classCache.put(new CacheKey(classLoader, factoryId), "");
        }
        return null;
    }

    private static class CacheKey {
        private final ClassLoader loader;
        private final String className;

        private CacheKey(final ClassLoader loader, final String className) {
            this.loader = loader;
            this.className = className;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final CacheKey cacheKey = (CacheKey) o;

            if (className != null ? !className.equals(cacheKey.className) : cacheKey.className != null) return false;
            if (loader != null ? !loader.equals(cacheKey.loader) : cacheKey.loader != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = loader != null ? loader.hashCode() : 0;
            result = 31 * result + (className != null ? className.hashCode() : 0);
            return result;
        }
    }


}
