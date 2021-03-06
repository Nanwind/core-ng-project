package core.framework.http;

import core.framework.util.Charsets;
import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public final class HTTPRequest {
    public static HTTPRequest get(String uri) {
        return new HTTPRequest(HTTPMethod.GET, uri);
    }

    public static HTTPRequest post(String uri) {
        return new HTTPRequest(HTTPMethod.POST, uri);
    }

    public static HTTPRequest put(String uri) {
        return new HTTPRequest(HTTPMethod.PUT, uri);
    }

    public static HTTPRequest delete(String uri) {
        return new HTTPRequest(HTTPMethod.DELETE, uri);
    }

    public static HTTPRequest patch(String uri) {
        return new HTTPRequest(HTTPMethod.PATCH, uri);
    }

    private final String uri;
    private final HTTPMethod method;
    private final Map<String, String> headers = Maps.newLinkedHashMap();
    private final Map<String, String> params = Maps.newLinkedHashMap();
    private ContentType contentType;
    private byte[] body;

    public HTTPRequest(HTTPMethod method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public String uri() {
        return uri;
    }

    public HTTPMethod method() {
        return method;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public void accept(ContentType contentType) {
        header(HTTPHeaders.ACCEPT, contentType.toString());
    }

    public void header(String name, String value) {
        headers.put(name, value);
    }

    public Map<String, String> params() {
        return params;
    }

    public void addParam(String name, String value) {
        params.put(name, value);
    }

    public byte[] body() {
        return body;
    }

    public void body(String body, ContentType contentType) {
        byte[] bytes = body.getBytes(contentType.charset().orElse(Charsets.UTF_8));
        body(bytes, contentType);
    }

    public void body(byte[] body, ContentType contentType) {
        this.body = body;
        this.contentType = contentType;
    }

    public ContentType contentType() {
        return contentType;
    }
}
