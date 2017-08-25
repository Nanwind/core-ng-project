package core.framework.impl.asm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
public class TestBean {
    public Integer intField;
    public Long longField;
    public String stringField;

    public LocalDateTime dateTimeField;
    public LocalDate dateField;
    public ZonedDateTime zonedDateTimeField;

    public TestEnum enumField;

    public enum TestEnum {
        VALUE1,
        VALUE2
    }
}
