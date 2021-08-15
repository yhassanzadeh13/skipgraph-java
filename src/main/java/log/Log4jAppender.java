package log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/** Appender implementation for Log4jLogger. */
public class Log4jAppender implements Appender {

  private JsonMessage message;
  private Logger logger;
  private Level level;
  private Throwable exception = null;

  /**
   * Constructor forLog4jAppender.
   *
   * @param logger logger instance.
   * @param level log level.
   */
  public Log4jAppender(Logger logger, Level level) {
    this.message = new JsonMessage();
    this.logger = logger;
    this.level = level;
  }

  /**
   * Constructor forLog4jAppender.
   *
   * @param msg Initial json message.
   * @param logger logger instance.
   * @param level log level.
   */
  public Log4jAppender(JsonMessage msg, Logger logger, Level level) {
    this.message = msg;
    this.logger = logger;
    this.level = level;
  }

  private Appender append(String key, Object value) {
    this.message.add(key, value);
    return this;
  }

  @Override
  public Appender addInt(String key, int value) {
    return this.append(key, value);
  }

  @Override
  public Appender addStr(String key, String value) {
    return this.append(key, value);
  }

  @Override
  public Appender addFloat(String key, float value) {
    return this.append(key, value);
  }

  @Override
  public Appender addDouble(String key, double value) {
    return this.append(key, value);
  }

  @Override
  public Appender addException(Exception e) {
    this.exception = e;
    return this;
  }

  @Override
  public void addMsg(String value) {
    this.message.add("msg", value);
    this.logger.log(this.level, this.message.toObjectMessage(), this.exception);
  }
}
