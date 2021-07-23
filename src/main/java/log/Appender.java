package log;

/**
 * Appender interface for adding multiple key-value pairs to JsonMessage.
 */
public interface Appender {

  Appender Int(String key, int value);

  Appender Str(String key, String value);

  Appender Float(String key, float value);

  Appender Double(String key, double value);

  Appender Exception(Exception e);

  void Msg(String value);
}
