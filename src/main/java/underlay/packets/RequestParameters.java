package underlay.packets;

import java.io.Serializable;

/**
 * Contains the set of key value pairs of a request sent by the client.
 */
public interface RequestParameters extends Serializable {
    Object getRequestValue(String parameterName);
}
