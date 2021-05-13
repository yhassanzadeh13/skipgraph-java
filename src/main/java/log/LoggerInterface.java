package log;

public interface LoggerInterface {
    Appender debug();
    Appender info();
    Appender warn();
    Appender error();
    Appender fatal();
    Appender trace();
}
