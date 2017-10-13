package core.framework.impl.web.request;

import core.framework.api.json.Property;
import core.framework.api.web.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import javax.xml.bind.annotation.XmlEnumValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class URLParamParserTest {
    @Test
    void failedToParseEnum() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> URLParamParser.parse("V2", TestEnum.class));
        assertThat(exception.getMessage(), containsString("failed to parse"));
    }

    @Test
    void parseBoolean() {
        assertTrue(URLParamParser.parse("true", Boolean.class));
    }

    @Test
    void parseEnum() {
        TestEnum value = URLParamParser.parse("V1", TestEnum.class);
        assertEquals(TestEnum.VALUE, value);
    }

    enum TestEnum {
        @Property(name = "V1")
        VALUE
    }
}
