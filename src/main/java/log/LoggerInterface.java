package log;

/**
 * Logger interface used for logging statements in key-value form.
 */
public interface LoggerInterface {

  Appender debug();

  Appender info();

  Appender warn();

  Appender error();

  Appender fatal();

  Appender trace();
}
