package core.framework.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * @author neo
 */
public class IntegrationExtension implements TestInstancePostProcessor {
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        TestManager manager = TestManager.get();
        manager.init(context.getTestClass().get());
        manager.injectTest(testInstance);
    }
}
