package core.framework.impl.redis.v2;

import core.framework.impl.log.LogParam;
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.redis.Redis;
import core.framework.redis.RedisHash;
import core.framework.redis.RedisSet;
import core.framework.util.Charsets;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author neo
 */
public class RedisImplV2 implements Redis {
    private static final byte[] NX = Strings.bytes("NX");
    private static final byte[] EX = Strings.bytes("EX");
    public final Pool<RedisConnection> pool;
    private final Logger logger = LoggerFactory.getLogger(core.framework.impl.redis.RedisImpl.class);
    private final String name;
    private String host;
    private long slowOperationThresholdInNanos = Duration.ofMillis(500).toNanos();
    private Duration timeout;

    public RedisImplV2(String name) {
        this.name = name;
        pool = new Pool<>(this::createConnection, name);
        pool.size(5, 50);
        pool.maxIdleTime(Duration.ofMinutes(30));
        timeout(Duration.ofSeconds(5));
    }

    public void host(String host) {
        this.host = host;
    }

    public void timeout(Duration timeout) {
        this.timeout = timeout;
        pool.checkoutTimeout(timeout);
    }

    public void slowOperationThreshold(Duration slowOperationThreshold) {
        slowOperationThresholdInNanos = slowOperationThreshold.toNanos();
    }

    private RedisConnection createConnection() {
        if (host == null) throw new Error("redis.host must not be null");
        try {
            return new RedisConnection(host, Protocol.DEFAULT_PORT, (int) timeout.toMillis());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void close() {
        logger.info("close redis client, name={}, host={}", name, host);
        pool.close();
    }

    @Override
    public String get(String key) {
        return decode(getBytes(key));
    }

    public byte[] getBytes(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.sendRequest(Protocol.Command.GET, encode(key));
            return connection.getBulkStringResponse();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("get, key={}, elapsedTime={}", key, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void set(String key, String value) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.sendRequest(Protocol.Command.SET, encode(key), encode(value));
            connection.getSimpleStringResponse();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("set, key={}, value={}, elapsedTime={}", key, value, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void set(String key, String value, Duration expiration) {
        set(key, encode(value), expiration);
    }

    public void set(String key, byte[] value, Duration expiration) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.sendRequest(Protocol.Command.SETEX, encode(key), encode(expiration.getSeconds()), value);
            connection.getSimpleStringResponse();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("set, key={}, value={}, expiration={}, elapsedTime={}", key, LogParam.of(value), expiration, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public RedisSet set() {
        return null;
    }

    @Override
    public boolean setIfAbsent(String key, String value, Duration expiration) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.sendRequest(Protocol.Command.SET, encode(key), encode(value), NX, EX, encode(expiration.getSeconds()));
            String result = connection.getSimpleStringResponse();
            return "OK".equals(result);
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("setIfAbsent, key={}, value={}, expiration={}, elapsedTime={}", key, value, expiration, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void expire(String key, Duration expiration) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.sendRequest(Protocol.Command.EXPIRE, encode(key), encode(expiration.getSeconds()));
            connection.getLongResponse();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("expire, key={}, expiration={}, elapsedTime={}", key, expiration, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void del(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.sendRequest(Protocol.Command.DEL, encode(key));
            connection.getLongResponse();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("del, key={}, elapsedTime={}", key, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Map<String, String> multiGet(String... keys) {
        Map<String, byte[]> values = multiGetBytes(keys);
        Map<String, String> result = Maps.newHashMapWithExpectedSize(values.size());
        for (Map.Entry<String, byte[]> entry : values.entrySet()) {
            result.put(entry.getKey(), decode(entry.getValue()));
        }
        return result;
    }

    public Map<String, byte[]> multiGetBytes(String... keys) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            byte[][] redisKeys = encode(keys);
            Map<String, byte[]> values = Maps.newHashMapWithExpectedSize(keys.length);
            connection.sendRequest(Protocol.Command.MGET, redisKeys);
            Object[] redisValues = connection.getArrayResponse();
            int index = 0;
            for (Object redisValue : redisValues) {
                if (redisValue != null) values.put(keys[index], (byte[]) redisValue);
                index++;
            }
            return values;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("mget, keys={}, elapsedTime={}", keys, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void multiSet(Map<String, String> values) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            byte[][] keyValues = new byte[values.size() * 2][];
            int i = 0;
            for (Map.Entry<String, String> entry : values.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                keyValues[i] = encode(key);
                keyValues[i + 1] = encode(value);
                i = i + 2;
            }
            connection.sendRequest(Protocol.Command.MSET, keyValues);
            connection.getSimpleStringResponse();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("mset, values={}, elapsedTime={}", values, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    public void multiSet(Map<String, byte[]> values, Duration expiration) {
        StopWatch watch = new StopWatch();
        byte[] expirationValue = encode((int) expiration.getSeconds());
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            for (Map.Entry<String, byte[]> entry : values.entrySet()) {
                connection.sendRequest(Protocol.Command.SETEX, encode(entry.getKey()), expirationValue, entry.getValue());
            }
            for (int i = 0; i < values.size(); i++) {
                connection.getSimpleStringResponse();
            }
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("mset, values={}, expiration={}, elapsedTime={}", LogParam.of(values), expiration, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public RedisHash hash() {
        return null;
    }

    @Override
    public void forEach(String pattern, Consumer<String> consumer) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        int count = 0;
        try {
            RedisConnection connection = item.resource;
            byte[] batchSize = encode("500"); // use 500 as batch
            String cursor = "0";
            do {
                connection.sendRequest(Protocol.Command.SCAN, encode(cursor), Protocol.Keyword.MATCH.value, encode(pattern), Protocol.Keyword.COUNT.value, batchSize);
                Object[] response = connection.getArrayResponse();
                cursor = decode((byte[]) response[0]);
                Object[] keys = (Object[]) response[1];
                for (Object key : keys) {
                    consumer.accept(decode((byte[]) key));
                }
                count += keys.length;
            } while (!"0".equals(cursor));
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("forEach, pattern={}, count={}, elapsedTime={}", pattern, count, elapsedTime);
        }
    }

    private byte[] encode(long number) {
        return Strings.bytes(String.valueOf(number));
    }

    byte[] encode(String value) {   // redis does not accept null
        return Strings.bytes(value);
    }

    byte[][] encode(String[] values) {
        int size = values.length;
        byte[][] redisValues = new byte[size][];
        for (int i = 0; i < size; i++) {
            redisValues[i] = encode(values[i]);
        }
        return redisValues;
    }

    String decode(byte[] value) {
        if (value == null) return null;
        return new String(value, Charsets.UTF_8);
    }

    void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_REDIS"), "slow redis operation, elapsedTime={}", elapsedTime);
        }
    }
}
