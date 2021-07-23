package log;

/**
 * Appender interface for adding multiple key-value pairs to JsonMessage.
 */
public interface Appender {

  Appender addInt(String key, int value);

  Appender addStr(String key, String value);

  Appender addFloat(String key, float value);

  Appender addDouble(String key, double value);

  Appender addException(Exception e);

  void addMsg(String value);
}
