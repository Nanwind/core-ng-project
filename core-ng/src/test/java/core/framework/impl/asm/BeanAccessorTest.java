package core.framework.impl.asm;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class BeanAccessorTest {
    private BeanAccessor accessor;

    @Before
    public void createBeanAccessor() {
        accessor = new BeanAccessor(TestBean.class);
    }

    @Test
    public void get() {
        TestBean bean = new TestBean();
        bean.intField = 28;
        bean.longField = 38L;
        bean.stringField = "value";
        bean.dateTimeField = LocalDateTime.now();
        bean.dateField = LocalDate.now();
        bean.zonedDateTimeField = ZonedDateTime.now();
        bean.enumField = TestBean.TestEnum.VALUE1;

        assertEquals(bean.intField, accessor.fields.get("intField").accessor.get(bean));
        assertEquals(bean.longField, accessor.fields.get("longField").accessor.get(bean));
        assertEquals(bean.stringField, accessor.fields.get("stringField").accessor.get(bean));
        assertEquals(bean.dateTimeField, accessor.fields.get("dateTimeField").accessor.get(bean));
        assertEquals(bean.dateField, accessor.fields.get("dateField").accessor.get(bean));
        assertEquals(bean.zonedDateTimeField, accessor.fields.get("zonedDateTimeField").accessor.get(bean));
        assertEquals(bean.enumField, accessor.fields.get("enumField").accessor.get(bean));
    }

    @Test
    public void set() {
        TestBean bean = new TestBean();
        accessor.fields.get("intField").accessor.set(bean, 28);
        accessor.fields.get("longField").accessor.set(bean, 38L);
        accessor.fields.get("stringField").accessor.set(bean, "value");
        LocalDateTime dateTime = LocalDateTime.of(2017, 8, 25, 16, 0, 0);
        accessor.fields.get("dateTimeField").accessor.set(bean, dateTime);
        LocalDate date = dateTime.toLocalDate();
        accessor.fields.get("dateField").accessor.set(bean, date);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
        accessor.fields.get("zonedDateTimeField").accessor.set(bean, zonedDateTime);
        accessor.fields.get("enumField").accessor.set(bean, TestBean.TestEnum.VALUE2);

        assertEquals(28, (int) bean.intField);
        assertEquals(38L, (long) bean.longField);
        assertEquals("value", bean.stringField);
        assertEquals(dateTime, bean.dateTimeField);
        assertEquals(date, bean.dateField);
        assertEquals(zonedDateTime, bean.zonedDateTimeField);
    }
}
