package core.framework.impl.asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.security.AccessController;
import java.security.PrivilegedAction;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * @author neo
 */
public class BeanAccessor {
    private static final BeanAccessorLoader CLASS_LOADER = AccessController.doPrivileged((PrivilegedAction<BeanAccessorLoader>) () -> new BeanAccessorLoader(BeanAccessor.class.getClassLoader()));

    public final Field[] fields;

    public BeanAccessor(Class<?> beanClass) {
        java.lang.reflect.Field[] fields = beanClass.getFields();
        this.fields = new Field[fields.length];
        for (int i = 0; i < fields.length; i++) {
            java.lang.reflect.Field field = fields[i];
            this.fields[i] = new Field(field.getName(), field.getType(), buildAccessor(beanClass, field));
        }
    }

    private FieldAccessor buildAccessor(Class<?> beanClass, java.lang.reflect.Field field) {
        ClassWriter writer = new ClassWriter(0);

        String beanType = asmType(beanClass);
        String fieldName = field.getName();
        String accessorType = beanType + "$" + fieldName + "Accessor";
        String fieldType = asmType(field.getType());

        writer.visit(52, ACC_PUBLIC + ACC_SUPER, accessorType, null, "java/lang/Object", new String[]{asmType(FieldAccessor.class)});
        buildConstructor(writer, accessorType);
        buildGetter(writer, beanType, accessorType, fieldType, fieldName);
        buildSetter(writer, beanType, accessorType, fieldType, fieldName);
        writer.visitEnd();

        byte[] classBytes = writer.toByteArray();
        Class<?> accessorClass = CLASS_LOADER.defineClassForName(accessorType.replace('/', '.'), classBytes);
        try {
            return (FieldAccessor) accessorClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    private void buildSetter(ClassWriter writer, String beanType, String accessorType, String fieldType, String fieldName) {
        MethodVisitor visitor = writer.visitMethod(ACC_PUBLIC, "set", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
        visitor.visitCode();
        Label l0 = new Label();
        visitor.visitLabel(l0);
        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitTypeInsn(CHECKCAST, beanType);
        visitor.visitVarInsn(ALOAD, 2);
        visitor.visitTypeInsn(CHECKCAST, fieldType);
        visitor.visitFieldInsn(PUTFIELD, beanType, fieldName, "L" + fieldType + ";");
        Label l1 = new Label();
        visitor.visitLabel(l1);
        visitor.visitInsn(RETURN);
        Label l2 = new Label();
        visitor.visitLabel(l2);
        visitor.visitLocalVariable("this", "L" + accessorType + ";", null, l0, l2, 0);
        visitor.visitLocalVariable("bean", "Ljava/lang/Object;", null, l0, l2, 1);
        visitor.visitLocalVariable("value", "Ljava/lang/Object;", null, l0, l2, 2);
        visitor.visitMaxs(2, 3);
        visitor.visitEnd();
    }

    private void buildGetter(ClassWriter writer, String beanType, String accessorType, String fieldType, String fieldName) {
        MethodVisitor visitor = writer.visitMethod(ACC_PUBLIC, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        visitor.visitCode();
        Label l0 = new Label();
        visitor.visitLabel(l0);
        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitTypeInsn(CHECKCAST, beanType);
        visitor.visitFieldInsn(GETFIELD, beanType, fieldName, "L" + fieldType + ";");
        visitor.visitInsn(ARETURN);
        Label l1 = new Label();
        visitor.visitLabel(l1);
        visitor.visitLocalVariable("this", "L" + accessorType + ";", null, l0, l1, 0);
        visitor.visitLocalVariable("bean", "Ljava/lang/Object;", null, l0, l1, 1);
        visitor.visitMaxs(1, 2);
        visitor.visitEnd();
    }

    private void buildConstructor(ClassWriter writer, String accessorClassName) {
        MethodVisitor visitor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        visitor.visitCode();
        Label l0 = new Label();
        visitor.visitLabel(l0);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        visitor.visitInsn(RETURN);
        Label l1 = new Label();
        visitor.visitLabel(l1);
        visitor.visitLocalVariable("this", "L" + accessorClassName + ";", null, l0, l1, 0);
        visitor.visitMaxs(1, 1);
        visitor.visitEnd();
    }

    private String asmType(Class<?> instanceClass) {
        return instanceClass.getName().replace('.', '/');
    }

    public static class Field {
        public final String name;
        public final Class<?> type;
        public final FieldAccessor accessor;

        public Field(String name, Class<?> type, FieldAccessor accessor) {
            this.name = name;
            this.type = type;
            this.accessor = accessor;
        }
    }
}
