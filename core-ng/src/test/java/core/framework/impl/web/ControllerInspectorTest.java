package core.framework.impl.web;

import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class ControllerInspectorTest {
    @Test
    void methodReference() throws NoSuchMethodException {
        ControllerInspector inspector = new ControllerInspector(new TestControllers()::get);
        assertEquals(TestControllers.class, inspector.targetClass);
        assertEquals(TestControllers.class.getDeclaredMethod("get", Request.class), inspector.targetMethod);
        assertEquals(TestControllers.class.getCanonicalName() + ".get", inspector.controllerInfo);
    }

    @Test
    void lambdaMethod() {
        ControllerInspector inspector = new ControllerInspector(request -> null);
        assertTrue(inspector.targetClass.getCanonicalName().startsWith(ControllerInspectorTest.class.getCanonicalName()));
        assertNotNull(inspector.targetMethod);
        assertTrue(inspector.controllerInfo.startsWith(ControllerInspectorTest.class.getCanonicalName() + "."));
    }

    @Test
    void staticClass() throws NoSuchMethodException {
        ControllerInspector inspector = new ControllerInspector(new TestController());
        assertEquals(TestController.class, inspector.targetClass);
        assertEquals(TestController.class.getMethod("execute", Request.class), inspector.targetMethod);
        assertEquals(TestController.class.getCanonicalName() + ".execute", inspector.controllerInfo);
    }

    public static class TestController implements Controller {
        @Override
        public Response execute(Request request) throws Exception {
            return null;
        }
    }

    public static class TestControllers {
        public Response get(Request request) {
            return null;
        }
    }
}
