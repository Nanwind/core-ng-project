package core.framework.impl.search;

import core.framework.api.search.Index;
import org.junit.jupiter.api.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
class DocumentClassValidatorTest {
    @Test
    void validate() {
        new DocumentClassValidator(TestDocument.class).validate();
    }

    @Index(index = "main", type = "test")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestDocument {
        @XmlElement(name = "date_time_field")
        public LocalDateTime dateTimeField;

        @XmlElement(name = "string_field")
        public String stringField;

        @XmlElement(name = "list_field")
        public List<String> listField;

        @XmlElement(name = "map_field")
        public Map<String, String> mapField;
    }
}
