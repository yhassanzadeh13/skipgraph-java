package log;

import org.apache.logging.log4j.message.ObjectMessage;

import java.util.HashMap;

public class JsonMessage {
    private HashMap<String, Object> data;

    public JsonMessage() {
        this.data = new HashMap<>();
    }

    public JsonMessage add(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public ObjectMessage toObjectMessage() {
        return new ObjectMessage(this.data);
    }
}