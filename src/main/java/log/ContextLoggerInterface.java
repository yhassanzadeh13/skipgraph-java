package log;

/**
 * Logger interface extended with context info in mind.
 */
public interface ContextLoggerInterface extends LoggerInterface {

  ContextLoggerInterface addInt(String key, int value);

  ContextLoggerInterface addStr(String key, String value);

  ContextLoggerInterface addFloat(String key, float value);

  ContextLoggerInterface addDouble(String key, double value);
}
