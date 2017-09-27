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
    private static final Method GET_CONSTANT_POOL;
    private static final Method CONTROLLER_METHOD;
    private static final int JDK_9_MINOR_VERSION;
    private static final int JDK_8_MINOR_VERSION;

    static {
        try {
            GET_CONSTANT_POOL = Class.class.getDeclaredMethod("getConstantPool");
//            AccessController.doPrivileged((PrivilegedAction<Method>) () -> {
//                GET_CONSTANT_POOL.setAccessible(true);
//                return GET_CONSTANT_POOL;
//            });
            CONTROLLER_METHOD = Controller.class.getDeclaredMethod("execute", Request.class);
        } catch (NoSuchMethodException e) {
            throw new Error("failed to initialize controller inspector, please contact arch team", e);
        }

        String jdkVersion = System.getProperty("java.version");
        if ("9".equals(jdkVersion)) {
            JDK_9_MINOR_VERSION = 0;
            JDK_8_MINOR_VERSION = -1;
        } else if (jdkVersion.startsWith("1.8.0_")) {
            JDK_9_MINOR_VERSION = -1;
            JDK_8_MINOR_VERSION = Integer.parseInt(jdkVersion.substring(6));
        } else {
            throw Exceptions.error("unsupported jdk version, please contact arch team, jdk={}", jdkVersion);
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
                Field overrideField = AccessibleObject.class.getDeclaredField("override");
                Unsafe unsafe = AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>) () -> {
                    final Field f = Unsafe.class.getDeclaredField("theUnsafe");
                    f.setAccessible(true);
                    return (Unsafe) f.get(null);
                });
                long overrideFieldOffset = unsafe.objectFieldOffset(overrideField);
                unsafe.putBoolean(GET_CONSTANT_POOL, overrideFieldOffset, true);
                Object constantPool = GET_CONSTANT_POOL.invoke(controllerClass); // constantPool is sun.reflect.ConstantPool, it can be changed in future JDK
                Method getSize = constantPool.getClass().getMethod("getSize");
                unsafe.putBoolean(getSize, overrideFieldOffset, true);
                int size = (int) getSize.invoke(constantPool);
                Method getMemberRefInfoAt = constantPool.getClass().getMethod("getMemberRefInfoAt", int.class);
                unsafe.putBoolean(getMemberRefInfoAt, overrideFieldOffset, true);
                String[] methodRefInfo = (String[]) getMemberRefInfoAt.invoke(constantPool, methodRefIndex(size));
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

    private int methodRefIndex(int size) {
        if (JDK_9_MINOR_VERSION >= 0) return size - 3;
        if (JDK_8_MINOR_VERSION >= 60)
            return size - 3; // from 1.8.0_60, the index of methodRefInfo is different from previous jdk
        return size - 2;
    }
}
