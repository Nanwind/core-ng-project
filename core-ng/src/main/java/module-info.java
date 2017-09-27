/**
 * @author neo
 */
module core.framework {
    requires core.framework.api;
    requires jdk.unsupported;
    requires javax.inject;
    requires jaxb.api;
    requires slf4j.api;
    requires jackson.core;
    requires jackson.databind;
    requires mongo.java.driver;
    requires elasticsearch;
}
