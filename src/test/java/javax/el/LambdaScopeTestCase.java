package javax.el;

import org.junit.Assert;
import org.junit.Test;

/**
 * testcase for https://issues.jboss.org/browse/JBEE-158
 *
 * @author Tomaz Cerar (c) 2015 Red Hat Inc.
 */

public class LambdaScopeTestCase {

    @Test
    public void testScope() {
        ELProcessor processor = new ELProcessor();
        processor.defineBean("x", null);
        try {
            processor.eval("(x -> x.bug ()) ('bug')");
        } catch (RuntimeException exception) {
            // This is expected, there is no method bug() on strings.

        }
        Assert.assertNull(processor.eval("x")); // This must evaluate to null, but instead evaluates to "bug".

    }
}
