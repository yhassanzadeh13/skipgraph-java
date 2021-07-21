package underlay.packets;

import java.io.Serializable;

/**
 * Represents a serializable response packet. Every response type must inherit from this class.
 */
public class Response implements Serializable {

  public final boolean locked;

  public Response() {
    this.locked = false;
  }

  public Response(boolean locked) {
    this.locked = locked;
  }
}

