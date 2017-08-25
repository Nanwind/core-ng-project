package core.framework.impl.asm;

/**
 * @author neo
 */
class BeanAccessorLoader extends ClassLoader {
    BeanAccessorLoader(ClassLoader loader) {
        super(loader);
    }

    Class<?> defineClassForName(String name, byte[] data) {
        return this.defineClass(name, data, 0, data.length);
    }
}
