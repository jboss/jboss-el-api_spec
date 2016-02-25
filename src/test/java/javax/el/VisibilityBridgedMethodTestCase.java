package javax.el;

import org.junit.Assert;
import org.junit.Test;

/**
 * Testcase for https://issues.jboss.org/browse/WFLY-6280
 *
 * @author Trond G. Ziarkowski
 */
public class VisibilityBridgedMethodTestCase {

    static class Parent {
        public String methodToCall() {
            return "value";
        }
    }

    public static class Child extends Parent {
        // methodToCall() will be bridged by compiler 
    }

    @Test
    public void testVisibilityBridgedMethodCall() {
        ELProcessor processor = new ELProcessor();
        processor.defineBean("child", new Child());
        processor.eval("val = child.methodToCall()");

        Assert.assertEquals("value", processor.eval("val"));
    }
}
