package core.framework.impl.asm;

import org.junit.BeforeClass;
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
    private static BeanAccessor accessor;

    @BeforeClass
    public static void createBeanAccessor() {
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

        assertEquals(bean.intField, accessor.fields[0].accessor.get(bean));
        assertEquals(bean.longField, accessor.fields[1].accessor.get(bean));
        assertEquals(bean.stringField, accessor.fields[2].accessor.get(bean));
        assertEquals(bean.dateTimeField, accessor.fields[3].accessor.get(bean));
        assertEquals(bean.dateField, accessor.fields[4].accessor.get(bean));
        assertEquals(bean.zonedDateTimeField, accessor.fields[5].accessor.get(bean));
        assertEquals(bean.enumField, accessor.fields[6].accessor.get(bean));
    }

    @Test
    public void set() {
        TestBean bean = new TestBean();
        accessor.fields[0].accessor.set(bean, 28);
        accessor.fields[1].accessor.set(bean, 38L);
        accessor.fields[2].accessor.set(bean, "value");
        LocalDateTime dateTime = LocalDateTime.of(2017, 8, 25, 16, 0, 0);
        accessor.fields[3].accessor.set(bean, dateTime);
        LocalDate date = dateTime.toLocalDate();
        accessor.fields[4].accessor.set(bean, date);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
        accessor.fields[5].accessor.set(bean, zonedDateTime);
        accessor.fields[6].accessor.set(bean, TestBean.TestEnum.VALUE2);

        assertEquals(28, (int) bean.intField);
        assertEquals(38L, (long) bean.longField);
        assertEquals("value", bean.stringField);
        assertEquals(dateTime, bean.dateTimeField);
        assertEquals(date, bean.dateField);
        assertEquals(zonedDateTime, bean.zonedDateTimeField);
    }
}
