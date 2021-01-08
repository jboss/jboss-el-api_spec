package jakarta.el;

import org.junit.Assert;
import org.junit.Test;

import wfly6280.Child;

/**
 * Testcase for https://issues.jboss.org/browse/WFLY-6280
 *
 * @author Trond G. Ziarkowski
 */
public class VisibilityBridgedMethodTestCase {

    @Test
    public void testVisibilityBridgedMethodCall() {
        ELProcessor processor = new ELProcessor();
        processor.defineBean("child", new Child<String, Integer>());
        processor.eval("val = child.methodToCall()");

        Assert.assertEquals("value", processor.eval("val"));
    }

    @Test
    public void testVisibilityBridgedMethodCallWithGenerics() {
      ELProcessor processor = new ELProcessor();
      processor.defineBean("child", new Child<String, Integer>());
      // Ensure that test is successful with multiple invocations
      for (int i = 0; i < 100; i++) {
          processor.eval("child.values('X')");
      }
    }
}
