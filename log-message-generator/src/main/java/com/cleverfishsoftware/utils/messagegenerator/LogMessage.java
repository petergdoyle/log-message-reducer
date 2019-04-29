/*
 */
package com.cleverfishsoftware.utils.messagegenerator;

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
    private final String body;

    public enum Level {
        trace,
        debug,
        warn,
        info,
        error,
        fatal;

        public static Level getRandomLevel(final Random r) {
            return values()[r.nextInt(values().length)];
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
        private final Logger logger;
        private final Level level;

        Builder(Logger logger, Level level, String body) {
            this.level = level;
            this.logger = logger;
            this.body = body;
        }

        public Builder addTag(final String key, final String value) {
            tags.put(key, value);
            return this;
        }

        void log() {
            LogMessage msg = new LogMessage(this);
            switch (level) {
                case trace:
                    logger.trace(serialize(msg));
                    break;
                case debug:
                    logger.debug(serialize(msg));
                    break;
                case warn:
                    logger.warn(serialize(msg));
                    break;
                case info:
                    logger.info(serialize(msg));
                    break;
                case error:
                    logger.error(serialize(msg));
                    break;
                case fatal:
                    logger.fatal(serialize(msg));
                    break;
                default:
                    throw new RuntimeException("this isn't supposed to fall thru");
            }
        }

        private String serialize(LogMessage msg) {
            JSONObject obj = new JSONObject();
            msg.tags.put("ts", OffsetDateTime.now(ZoneOffset.UTC).toString());
            msg.tags.put("body", msg.body);
            msg.tags.forEach((k, v) -> obj.put(k, v));
            String str = obj.toJSONString();
            return str;
        }

        Object validate() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
