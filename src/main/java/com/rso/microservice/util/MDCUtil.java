package com.rso.microservice.util;

import org.slf4j.MDC;

public class MDCUtil {

    public static void put(MDCUtilKey key, String value) {
        MDC.put(key.getValue(), value);
    }

    public static String get(MDCUtilKey key) {
        return MDC.get(key.getValue());
    }

    public static void putAll(String name, String version, String requestId) {
        MDC.put(MDCUtilKey.MICROSERVICE_NAME.getValue(), name);
        MDC.put(MDCUtilKey.MICROSERVICE_VERSION.getValue(), version);
        MDC.put(MDCUtilKey.REQUEST_ID.getValue(), requestId);
    }

    public static void clear() {
        MDC.clear();
    }

    public enum MDCUtilKey {
        MICROSERVICE_NAME("microservice-name"),
        MICROSERVICE_VERSION("microservice-version"),
        REQUEST_ID("microservice-request-id");

        private final String value;

        MDCUtilKey(String v) {
            this.value = v;
        }

        public String getValue() {
            return value;
        }
    }
}
