package log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Example usage:.
 *
 * <p>logger.debug(). <br>
 * Int("owner_num_id", this.owner.getNumID()). <br>
 * Float("string key", value). <br>
 * Double("key", value). <br>
 * Str("str", value). <br>
 * Msg("starting insertion");
 */
public class Log4jLogger implements LoggerInterface {

  private Logger logger;

  public Log4jLogger(Logger logger) {
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

  public ContextLoggerInterface with() {
    return new ContextLogger(this.logger);
  }
}
