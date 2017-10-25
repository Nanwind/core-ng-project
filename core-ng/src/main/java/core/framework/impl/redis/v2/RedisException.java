package core.framework.impl.redis.v2;

/**
 * @author neo
 */
public class RedisException extends RuntimeException {
    private static final long serialVersionUID = -5459336238321986524L;

    public RedisException(String message) {
        super(message);
    }
}
