package core.framework.impl.redis.v2;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author neo
 */
public class RedisInputStream {
    private final InputStream inputStream;
    private final byte[] buffer = new byte[8192];
    private int position, limit;

    public RedisInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public byte readByte() throws IOException {
        fill();
        return buffer[position++];
    }

    public String readSimpleString() throws IOException {
        StringBuilder builder = new StringBuilder();
        while (true) {
            fill();
            byte value1 = buffer[position++];
            if (value1 == '\r') {
                fill();
                byte value2 = buffer[position++];
                if (value2 == '\n') {
                    break;
                }
                builder.append((char) value1);
                builder.append((char) value2);
            } else {
                builder.append((char) value1);
            }
        }

        String response = builder.toString();
        if (response.length() == 0) {
            throw new IOException("simple string must not be empty");
        }
        return response;
    }

    public long readLongCRLF() throws IOException {
        fill();
        boolean negative = buffer[position] == '-';
        if (negative) {
            position++;
        }
        long value = 0;
        while (true) {
            fill();
            int byteValue = buffer[position++];
            if (byteValue == '\r') {
                fill();
                if (buffer[position++] != '\n') throw new IOException("unexpected character");
                break;
            } else {
                value = value * 10 + byteValue - '0';
            }
        }
        return (negative ? -value : value);
    }

    public byte[] readBulkStringCRLF(int length) throws IOException {
        byte[] response = new byte[length];
        int offset = 0;
        while (offset < length) {
            fill();
            int readLength = Math.min(limit - position, (length - offset));
            System.arraycopy(buffer, position, response, offset, readLength);
            position += readLength;
            offset += readLength;
        }
        byte value = readByte();
        if (value != '\r') throw new IOException("unexpected character");
        value = readByte();
        if (value != '\n') throw new IOException("unexpected character");
        return response;
    }

    private void fill() throws IOException {
        if (position >= limit) {
            limit = inputStream.read(buffer);
            position = 0;
            if (limit == -1) {
                throw new IOException("unexpected end of stream");
            }
        }
    }
}
