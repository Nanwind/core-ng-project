package core.framework.module;

import java.nio.file.Paths;

/**
 * @author neo
 */
public final class SystemModule extends Module {
    private final String propertyFileClasspath;

    public SystemModule(String propertyFileClasspath) {
        this.propertyFileClasspath = propertyFileClasspath;
    }

    @Override
    protected void initialize() {
        loadProperties(propertyFileClasspath);

        configureHTTP();

        property("sys.cache.host").ifPresent(host -> {
            if ("local".equals(host)) {
                cache().local();
            } else {
                cache().redis(host);
            }
        });

        property("sys.session.host").ifPresent(host -> {
            if ("local".equals(host)) {
                site().session().local();
            } else {
                site().session().redis(host);
            }
        });

        property("sys.cdn.host").ifPresent(host -> site().cdn().host(host));

        configureLog();

        property("sys.kafka.uri").ifPresent(uri -> kafka().uri(uri));

        configureDB();

        property("sys.redis.host").ifPresent(host -> redis().host(host));

        property("sys.elasticsearch.host").ifPresent(host -> search().host(host));

        property("sys.mongo.uri").ifPresent(uri -> mongo().uri(uri));
    }

    void configureHTTP() {
        property("sys.http.port").ifPresent(port -> http().httpPort(Integer.parseInt(port)));

        property("sys.https.port").ifPresent(port -> http().httpsPort(Integer.parseInt(port)));
    }

    private void configureLog() {
        property("sys.log.actionLogPath").ifPresent(path -> {
            if ("console".equals(path)) {
                log().writeActionLogToConsole();
            } else {
                log().writeActionLogToFile(Paths.get(path));
            }
        });
        property("sys.log.traceLogPath").ifPresent(path -> {
            if ("console".equals(path)) {
                log().writeTraceLogToConsole();
            } else {
                log().writeTraceLogToFile(Paths.get(path));
            }
        });
        property("sys.log.kafkaURI").ifPresent(uri -> log().forwardLog(uri));
    }

    private void configureDB() {
        property("sys.jdbc.url").ifPresent(url -> db().url(url));
        property("sys.jdbc.user").ifPresent(user -> db().user(user));
        property("sys.jdbc.password").ifPresent(password -> db().password(password));
    }
}
