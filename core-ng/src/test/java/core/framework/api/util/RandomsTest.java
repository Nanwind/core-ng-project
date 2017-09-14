package core.framework.api.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class RandomsTest {
    @Test
    void randomAlphaNumeric() {
        assertEquals(3, Randoms.alphaNumeric(3).length());
        assertEquals(5, Randoms.alphaNumeric(5).length());
        assertEquals(10, Randoms.alphaNumeric(10).length());
    }

    @Test
    void randomNumber() {
        double number = Randoms.number(8000, 12000);
        assertTrue(number >= 8000 && number < 12000);

        number = Randoms.number(0.8, 1.2);
        assertTrue(number >= 0.8 && number < 1.2);
    }
}
