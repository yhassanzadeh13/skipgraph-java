package log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Example usage:
 * <p>
 *     logger.debug().
 * 				<br>Int("owner_num_id", this.owner.getNumID()).
 * 				<br>Float("string key", value).
 * 				<br>Double("key", value).
 * 				<br>Str("str", value).
 * 				<br>Msg("starting insertion");
 * </p>
 */
public class Log4jLogger implements LoggerInterface {
    public class Log4jAppender implements Appender {
        private JsonMessage message;
        private Logger logger;
        private Level level;
        private Throwable e = null;

        public Log4jAppender(Logger logger, Level level) {
            this.message = new JsonMessage();
            this.logger = logger;
            this.level = level;
        }

        private Appender append(String key, Object value) {
            this.message.add(key, value);
            return this;
        }

        @Override
        public Appender Int(String key, int value) {
            return this.append(key, value);
        }

        @Override
        public Appender Str(String key, String value) {
            return this.append(key, value);
        }

        @Override
        public Appender Float(String key, float value) {
            return this.append(key, value);
        }

        @Override
        public Appender Double(String key, double value) {
            return this.append(key, value);
        }

        @Override
        public Appender Exception(Exception e) {
            this.e = e;
            return this;
        }

        @Override
        public void Msg(String value) {
            this.message.add("msg", value);
            this.logger.log(this.level, this.message.toObjectMessage(), this.e);
        }
    }
    private Logger logger;

    public Log4jLogger(Logger logger){
        this.logger = logger;
    }

    @Override
    public Appender debug() {
        return new Log4jAppender(this.logger, Level.DEBUG);
    }

    @Override
    public Appender info() {
        return new Log4jAppender(this.logger, Level.INFO);
    }

    @Override
    public Appender warn() {
        return new Log4jAppender(this.logger, Level.WARN);
    }

    @Override
    public Appender error() {
        return new Log4jAppender(this.logger, Level.ERROR);
    }

    @Override
    public Appender fatal() {
        return new Log4jAppender(this.logger, Level.FATAL);
    }

    @Override
    public Appender trace() {
        return new Log4jAppender(this.logger, Level.TRACE);
    }
}
