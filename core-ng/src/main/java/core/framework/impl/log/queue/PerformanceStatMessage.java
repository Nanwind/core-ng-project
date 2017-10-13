package core.framework.impl.log.queue;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public class PerformanceStatMessage {
    @Property(name = "total_elapsed")
    public Long totalElapsed;
    @Property(name = "count")
    public Integer count;
}
