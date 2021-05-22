package log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Example usage:
 * <p>
 *     Log4jLogger logger =  new Log4jLogger(LogManager.getLogger(Main.class));
 *
 *     ContextLoggerInterface lg = logger.With().Str("name", "Ozgur");
 *
 *     lg.debug().Int("name_id", 12).Msg("test 1"); -> {msg=test 1, name=Ozgur, name_id=12}
 * </p>
 */
public class ContextLogger implements  ContextLoggerInterface{
    private Logger logger;
    private JsonMessage msg = new JsonMessage();

    public ContextLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public ContextLoggerInterface Int(String key, int value) {
        this.msg.add(key, value);
        return this;
    }

    @Override
    public ContextLoggerInterface Str(String key, String value) {
        this.msg.add(key, value);
        return this;
    }

    @Override
    public ContextLoggerInterface Float(String key, float value) {
        this.msg.add(key, value);
        return this;
    }

    @Override
    public ContextLoggerInterface Double(String key, double value) {
        this.msg.add(key, value);
        return this;
    }

    @Override
    public Appender debug() {
        return new Log4jAppender(this.msg, this.logger, Level.DEBUG);
    }

    @Override
    public Appender info() {
        return new Log4jAppender(this.msg, this.logger, Level.INFO);
    }

    @Override
    public Appender warn() {
        return new Log4jAppender(this.msg, this.logger, Level.WARN);
    }

    @Override
    public Appender error() {
        return new Log4jAppender(this.msg, this.logger, Level.ERROR);
    }

    @Override
    public Appender fatal() {
        return new Log4jAppender(this.msg, this.logger, Level.FATAL);
    }

    @Override
    public Appender trace() {
        return new Log4jAppender(this.msg, this.logger, Level.TRACE);
    }
}
