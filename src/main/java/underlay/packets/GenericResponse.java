package underlay.packets;

import java.io.Serializable;
import java.util.HashMap;

public class GenericResponse implements ResponseParameters {

    private final HashMap<String, Serializable> map = new HashMap<>();

    public void addParameter(String key, Serializable value) {
        map.put(key, value);
    }

    @Override
    public Object getResponseValue(String key) {
        if(map.containsKey(key)) return map.get(key);
        return null;
    }
}
