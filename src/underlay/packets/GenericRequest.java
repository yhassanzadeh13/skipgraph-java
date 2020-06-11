package underlay.packets;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents a request with arbitrary key, value pairs. Used for testing.
 */
public class GenericRequest implements RequestParameters {

    private final HashMap<String, Serializable> map = new HashMap<>();

    public void addParameter(String key, Serializable value) {
        map.put(key, value);
    }

    @Override
    public Object getRequestValue(String key) {
        if(map.containsKey(key)) return map.get(key);
        return null;
    }
}
