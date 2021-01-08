/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2015, Red Hat, Inc., and individual contributors
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
package jakarta.el;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class BeanELResolverTestCase {
    public static class Greeter {
        public String greet(final String... args) {
            return "Hello " + Arrays.toString(args);
        }
        public String greet2(final String prefix, final String... args) {
            return "Hello " + prefix + ": " + Arrays.toString(args);
        }
    }

    @Test
    public void testVarArgs() {
        final BeanELResolver resolver = new BeanELResolver();
        final ELContext context = new StandardELContext(ExpressionFactory.newInstance());
        final Greeter base = new Greeter();
        final String method = "greet";
        final Class[] paramTypes = new Class[] { String[].class };
        final Object[] params = new Object[] { new String[] { "testVarArgs" } };
        final String result = (String) resolver.invoke(context, base, method, paramTypes, params);
        assertEquals("Hello [testVarArgs]", result);
    }

    @Test
    public void testVarArgs2() {
        final BeanELResolver resolver = new BeanELResolver();
        final ELContext context = new StandardELContext(ExpressionFactory.newInstance());
        final Greeter base = new Greeter();
        final String method = "greet2";
        final Class[] paramTypes = new Class[] { String.class, String[].class };
        final Object[] params = new Object[] { "prefix", new String[] { "testVarArgs2" } };
        final String result = (String) resolver.invoke(context, base, method, paramTypes, params);
        assertEquals("Hello prefix: [testVarArgs2]", result);
    }
    
    /**
     * original test from the bugfix
    */
    @Test
    public void testBug56425() {
        ELProcessor processor = new ELProcessor();
        processor.defineBean("string", "a-b-c-d");
        assertEquals("a_b_c_d", processor.eval("string.replace(\"-\",\"_\")"));
    }
    
    /**
     * test the bugfix following the pattern of other tests
     */
    @Test
    public void testBug56425_2() {
        final BeanELResolver resolver = new BeanELResolver();
        final ELContext context = new StandardELContext(ExpressionFactory.newInstance());
        final String base = "a-b-c-d";
        final String method = "replace";
        final Class[] paramTypes = new Class[] { String.class, String.class };
        final Object[] params = new Object[] { "-", "_" };
        final String result = (String) resolver.invoke(context, base, method, paramTypes, params);
        assertEquals("a_b_c_d", result);
	}
}
