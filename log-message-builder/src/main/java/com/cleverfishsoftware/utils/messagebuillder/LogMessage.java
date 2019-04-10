/*
 */
package com.cleverfishsoftware.utils.messagebuillder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.logging.log4j.Logger;

import org.json.simple.JSONObject;

/**
 *
 */
public class LogMessage {

    private final HashMap<String, String> tags;
    private String body;

    public enum Level {
        trace,
        debug,
        warn,
        info,
        error,
        fatal;
        public static Level getRandomLevel(final Random random) {
            return values()[random.nextInt(values().length)];
        }
    }

    private LogMessage(Builder builder) {
        this.body = builder.body;
        this.tags = builder.tags;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "LogMessage{" + "tags=" + tags + ", body=" + body + '}';
    }

    public static class Builder {

        private final HashMap<String, String> tags = new HashMap<>();
        private final String body;
        private final String ts;
        private final Logger logger;
        private Level level;
        private final OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);

        Builder(Logger logger, Level level, String body) {
            this.level = level;
            this.logger = logger;
            this.body = body;
            ts = Instant.now().toString();
        }

        public Builder addTag(final String key, final String value) {
            tags.put(key, value);
            return this;
        }

        void log() {
            LogMessage msg = new LogMessage(this);
            String serialized = serialize(msg);
            switch (level) {
                case trace:
                    logger.trace(serialized);
                    break;
                case debug:
                    logger.debug(serialized);
                    break;
                case warn:
                    logger.warn(serialized);
                    break;
                case info:
                    logger.info(serialized);
                    break;
                default:
                    logger.error(serialized);
            }
        }

        private String serialize(LogMessage msg) {
            JSONObject obj = new JSONObject();
            msg.tags.forEach((k, v) -> obj.put(k, v));
            obj.put("body", msg.body);
//            obj.put("ts", utc);
            String str = obj.toJSONString().toString();
            return str;
        }

        Object validate() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
