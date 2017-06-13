package org.slf4j;

public class LoggerFactory {

    public static Logger getLogger(Class<?> loggerClass) {
        return new FakeLogger();
    }

}
