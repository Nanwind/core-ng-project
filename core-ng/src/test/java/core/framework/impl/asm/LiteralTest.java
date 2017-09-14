package core.framework.impl.asm;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Types;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class LiteralTest {
    @Test
    void enumVariable() {
        assertEquals("core.framework.api.http.HTTPMethod.POST", Literal.variable(HTTPMethod.POST));
    }

    @Test
    void typeVariable() {
        assertEquals("java.lang.String.class", Literal.variable(String.class));

        assertEquals("core.framework.api.util.Types.list(java.lang.String.class)", Literal.variable(Types.list(String.class)));

        assertEquals("core.framework.api.util.Types.optional(java.lang.String.class)", Literal.variable(Types.optional(String.class)));
    }
}
