package core.framework.impl.web;

import core.framework.api.util.Exceptions;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import sun.misc.Unsafe;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * due to Java 8 doesn't provide formal way to reflect lambda method reference, we uses sun internal API for now
 * and wait for JDK update in future
 *
 * @author neo
 */
public class ControllerInspector {
    private static final Method CONTROLLER_METHOD;

    static {
        try {
            CONTROLLER_METHOD = Controller.class.getDeclaredMethod("execute", Request.class);
        } catch (NoSuchMethodException e) {
            throw new Error("failed to initialize controller inspector, please contact arch team", e);
        }

        int jdkVersion = Runtime.version().major();
        if (jdkVersion < 9) {
            throw Exceptions.error("unsupported jdk version, please contact arch team, version={}", jdkVersion);
        }
    }

    public final Class<?> targetClass;
    public final Method targetMethod;
    public final String controllerInfo;

    public ControllerInspector(Controller controller) {
        Class<?> controllerClass = controller.getClass();
        try {
            if (!controllerClass.isSynthetic()) {
                targetClass = controllerClass;
                targetMethod = controllerClass.getMethod(CONTROLLER_METHOD.getName(), CONTROLLER_METHOD.getParameterTypes());
                controllerInfo = controllerClass.getCanonicalName() + "." + CONTROLLER_METHOD.getName();
            } else {
                Method getConstantPool = Class.class.getDeclaredMethod("getConstantPool");
                overrideAccessible(getConstantPool);
                Object constantPool = getConstantPool.invoke(controllerClass); // constantPool is sun.reflect.ConstantPool, it can be changed in future JDK
                Method getSize = constantPool.getClass().getMethod("getSize");
                overrideAccessible(getSize);
                int size = (int) getSize.invoke(constantPool);
                Method getMemberRefInfoAt = constantPool.getClass().getMethod("getMemberRefInfoAt", int.class);
                overrideAccessible(getMemberRefInfoAt);
                String[] methodRefInfo = (String[]) getMemberRefInfoAt.invoke(constantPool, size - 3);
                Class<?> targetClass = Class.forName(methodRefInfo[0].replace('/', '.'));
                String targetMethodName = methodRefInfo[1];
                controllerInfo = targetClass.getCanonicalName() + "." + targetMethodName;
                if (targetMethodName.contains("$")) {   // for lambda
                    this.targetClass = controllerClass;
                    targetMethod = controllerClass.getMethod(CONTROLLER_METHOD.getName(), CONTROLLER_METHOD.getParameterTypes());
                } else {    // for method reference
                    this.targetClass = targetClass;
                    targetMethod = targetClass.getMethod(targetMethodName, CONTROLLER_METHOD.getParameterTypes());
                }
            }
        } catch (NoSuchMethodException | PrivilegedActionException | NoSuchFieldException | InvocationTargetException | ClassNotFoundException | IllegalAccessException e) {
            throw new Error("failed to inspect controller", e);
        }
    }

    private void overrideAccessible(Method method) throws PrivilegedActionException, NoSuchFieldException {
        Field overrideField = AccessibleObject.class.getDeclaredField("override");
        Unsafe unsafe = AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>) () -> {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        });
        long overrideFieldOffset = unsafe.objectFieldOffset(overrideField);
        unsafe.putBoolean(method, overrideFieldOffset, true);
    }
}
