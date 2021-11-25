package network.underlay.packets.responses;

import network.underlay.packets.Response;

/** Represents a simple boolean response. */
public class BooleanResponse extends Response {

  public final boolean answer;

  public BooleanResponse(boolean answer) {
    this.answer = answer;
  }
}
