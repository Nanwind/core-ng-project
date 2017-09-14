package core.framework.api.crypto;

import core.framework.api.util.Charsets;
import core.framework.api.util.Strings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class AESTest {
    @Test
    void encryptAndDecrypt() {
        byte[] key = AES.generateKey(128);
        AES aes = new AES(key);

        String message = "test-message";
        byte[] cipherText = aes.encrypt(Strings.bytes(message));
        byte[] plainBytes = aes.decrypt(cipherText);
        String plainText = new String(plainBytes, Charsets.UTF_8);

        assertEquals(message, plainText);
    }
}
