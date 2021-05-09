package misc;

import org.apache.logging.log4j.message.ObjectMessage;

public interface Appender {
    Appender Int(String key, int value);
    Appender Str(String key, String value);
    Appender Float(String key, float value);
    Appender Double(String key, double value);
    Appender Exception(Exception e);
    void Msg(String value);
}
