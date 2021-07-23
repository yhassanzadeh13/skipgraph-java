package underlay.packets.responses;

import lookup.TentativeTable;
import underlay.packets.Response;

/**
 * Response for table.
 */
public class TableResponse extends Response {

  public final TentativeTable table;

  public TableResponse(TentativeTable table) {
    this.table = table;
  }
}
