package org.jboss.el.cache;

import javax.el.ELException;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Stuart Douglas
 */
public class BeanPropertiesCache {

    static private class BPSoftReference extends SoftReference<BeanProperties> {
        final Class<?> key;
        BPSoftReference(Class<?> key, BeanProperties beanProperties,
                        ReferenceQueue<BeanProperties> refQ) {
            super(beanProperties, refQ);
            this.key = key;
        }
    }

    public static class SoftConcurrentHashMap extends
            ConcurrentHashMap<Class<?>, BeanProperties> {

        private static final int CACHE_INIT_SIZE = 1024;
        private ConcurrentHashMap<Class<?>, BPSoftReference> map =
                new ConcurrentHashMap<Class<?>, BPSoftReference>(CACHE_INIT_SIZE);
        private ReferenceQueue<BeanProperties> refQ =
                new ReferenceQueue<BeanProperties>();

        // Remove map entries that have been placed on the queue by GC.
        private void cleanup() {
            BPSoftReference BPRef = null;
            while ((BPRef = (BPSoftReference)refQ.poll()) != null) {
                map.remove(BPRef.key);
            }
        }

        protected void clear(ClassLoader classLoader) {
            Iterator<Map.Entry<Class<?>, BPSoftReference>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Class<?>, BPSoftReference> entry = it.next();
                if(entry.getKey().getClassLoader() == classLoader) {
                    it.remove();
                }
            }

        }

        @Override
        public BeanProperties put(Class<?> key, BeanProperties value) {
            cleanup();
            BPSoftReference prev =
                    map.put(key, new BPSoftReference(key, value, refQ));
            return prev == null? null: prev.get();
        }

        @Override
        public BeanProperties putIfAbsent(Class<?> key, BeanProperties value) {
            cleanup();
            BPSoftReference prev =
                    map.putIfAbsent(key, new BPSoftReference(key, value, refQ));
            return prev == null? null: prev.get();
        }

        @Override
        public BeanProperties get(Object key) {
            cleanup();
            BPSoftReference BPRef = map.get(key);
            if (BPRef == null) {
                return null;
            }
            if (BPRef.get() == null) {
                // value has been garbage collected, remove entry in map
                map.remove(key);
                return null;
            }
            return BPRef.get();
        }
    }
    /*
     * Defines a property for a bean.
     */
    public final static class BeanProperty {

        private Method readMethod;
        private Method writeMethod;
        private PropertyDescriptor descriptor;

        public BeanProperty(Class<?> baseClass,
                            PropertyDescriptor descriptor) {
            this.descriptor = descriptor;
            readMethod = getMethod(baseClass, descriptor.getReadMethod());
            writeMethod = getMethod(baseClass, descriptor.getWriteMethod());
        }

        public Class getPropertyType() {
            return descriptor.getPropertyType();
        }

        public boolean isReadOnly() {
            return getWriteMethod() == null;
        }

        public Method getReadMethod() {
            return readMethod;
        }

        public Method getWriteMethod() {
            return writeMethod;
        }
    }

    /*
     * Defines the properties for a bean.
     */
    public final static class BeanProperties {

        private final Map<String, BeanProperty> propertyMap =
                new HashMap<String, BeanProperty>();

        public BeanProperties(Class<?> baseClass) {
            PropertyDescriptor[] descriptors;
            try {
                BeanInfo info = Introspector.getBeanInfo(baseClass);
                descriptors = info.getPropertyDescriptors();
            } catch (IntrospectionException ie) {
                throw new ELException(ie);
            }
            for (PropertyDescriptor pd: descriptors) {
                propertyMap.put(pd.getName(),
                        new BeanProperty(baseClass, pd));
            }
        }

        public BeanProperty getBeanProperty(String property) {
            return propertyMap.get(property);
        }
    }

    /**
     * sfot references are horrible
     */
    private static final SoftConcurrentHashMap properties =
            new SoftConcurrentHashMap();


    /*
     * Get a public method form a public class or interface of a given method.
     * Note that if a PropertyDescriptor is obtained for a non-public class that
     * implements a public interface, the read/write methods will be for the
     * class, and therefore inaccessible.  To correct this, a version of the
     * same method must be found in a superclass or interface.
     **/

    public static Method getMethod(Class<?> cl, Method method) {

        if (method == null) {
            return null;
        }

        if (Modifier.isPublic(cl.getModifiers())) {
            return method;
        }
        Class<?> [] interfaces = cl.getInterfaces ();
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> c = interfaces[i];
            Method m = null;
            try {
                m = c.getMethod(method.getName(), method.getParameterTypes());
                c = m.getDeclaringClass();
                if ((m = getMethod(c, m)) != null)
                    return m;
            } catch (NoSuchMethodException ex) {
            }
        }
        Class<?> c = cl.getSuperclass();
        if (c != null) {
            Method m = null;
            try {
                m = c.getMethod(method.getName(), method.getParameterTypes());
                c = m.getDeclaringClass();
                if ((m = getMethod(c, m)) != null)
                    return m;
            } catch (NoSuchMethodException ex) {
            }
        }
        return null;
    }

    public static SoftConcurrentHashMap getProperties() {
        return properties;
    }

    static void clear(ClassLoader classLoader) {
        properties.clear(classLoader);
    }
}
