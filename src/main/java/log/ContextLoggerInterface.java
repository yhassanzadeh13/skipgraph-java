package log;

public interface ContextLoggerInterface extends LoggerInterface {

  ContextLoggerInterface Int(String key, int value);

  ContextLoggerInterface Str(String key, String value);

  ContextLoggerInterface Float(String key, float value);

  ContextLoggerInterface Double(String key, double value);
}
