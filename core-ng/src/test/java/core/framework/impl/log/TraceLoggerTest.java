package core.framework.impl.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class TraceLoggerTest {
    private TraceLogger logger;

    @BeforeEach
    void createTraceLogger() {
        logger = new TraceLogger(null);
    }

    @Test
    void traceLogFilePath() {
        String logFilePath = logger.traceLogFilePath("/log", LocalDateTime.of(2012, Month.OCTOBER, 2, 14, 5), "someController-method", "requestId");
        assertTrue(logFilePath.startsWith("/log/someController-method/201210021405.requestId."));
        assertTrue(logFilePath.endsWith(".log"));
    }
}
