package core.framework.impl.kafka;

import org.junit.jupiter.api.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
class MessageClassValidatorTest {
    @Test
    void validate() {
        new MessageClassValidator(TestMessage.class).validate();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestMessage {
        @XmlElement(name = "zoned_date_time_field")
        public ZonedDateTime zonedDateTimeField;

        @XmlElement(name = "date_time_field")
        public LocalDateTime dateTimeField;

        @XmlElement(name = "string_field")
        public String stringField;

        @XmlElement(name = "list_field")
        public List<String> listField;

        @XmlElement(name = "map_field")
        public Map<String, String> mapField;

        @XmlElement(name = "child_field")
        public TestChild childField;

        @XmlElement(name = "children_field")
        public List<TestChild> childrenField;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestChild {
        @XmlElement(name = "boolean_field")
        public Boolean booleanField;
    }
}
