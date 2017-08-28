package core.framework.impl.asm;

/**
 * @author neo
 */
public class BeanField {
    public final String name;
    public final Class<?> type;
    public final FieldAccessor accessor;

    public BeanField(String name, Class<?> type, FieldAccessor accessor) {
        this.name = name;
        this.type = type;
        this.accessor = accessor;
    }
}
