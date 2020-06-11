package underlay.packets;

/**
 * Represents an empty acknowledgement response. Used for testing.
 */
public class AckResponse implements ResponseParameters {

    @Override
    public Object getResponseValue(String parameterName) {
        return null;
    }
}
