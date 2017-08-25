package core.framework.impl.asm;

/**
 * @author neo
 */
public interface FieldAccessor {
    Object get(Object bean);

    void set(Object bean, Object value);
}
