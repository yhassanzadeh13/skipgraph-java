package log;

public interface Appender {

  Appender Int(String key, int value);

  Appender Str(String key, String value);

  Appender Float(String key, float value);

  Appender Double(String key, double value);

  Appender Exception(Exception e);

  void Msg(String value);
}
