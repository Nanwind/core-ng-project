package core.framework.impl.web.request;

import core.framework.api.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author neo
 */
class PathParamsTest {
    private PathParams pathParams;

    @BeforeEach
    void createPathParams() {
        pathParams = new PathParams();
    }

    @Test
    void putEmptyPathParam() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> pathParams.put("id", ""));
        assertThat(exception.getMessage(), containsString("name=id, value="));
    }
}
