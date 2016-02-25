package javax.el;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/**
 * Testcase for https://issues.jboss.org/browse/WFLY-3456
 *
 * @author Trond G. Ziarkowski
 */
public class NullParameterTestCase {

    public static interface SimpleDataTableHandler<T> {
        public Date getLastUpdate(T item);
    }
  
    public static class SimpleStringHandler implements SimpleDataTableHandler<String> {
        @Override
        public Date getLastUpdate(String item) {
            return new Date();
        }
    }

    @Test
    public void testAmbiguousMethodCall() {
        ELProcessor processor = new ELProcessor();
        processor.defineBean("handler", new SimpleStringHandler());
        processor.eval("date = handler.getLastUpdate(null)");

        Assert.assertNotNull(processor.eval("date"));
    }
}
