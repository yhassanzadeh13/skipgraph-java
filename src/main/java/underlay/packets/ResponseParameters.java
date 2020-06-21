package underlay.packets;

import java.io.Serializable;

/**
 * Contains the set of key value pairs of a response sent by the server.
 */
public interface ResponseParameters extends Serializable {
    Object getResponseValue(String parameterName);
}
