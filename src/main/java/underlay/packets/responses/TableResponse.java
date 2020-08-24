package underlay.packets.responses;

import lookup.TentativeTable;
import skipnode.SkipNodeIdentity;
import underlay.packets.Response;

import java.util.List;

public class TableResponse extends Response {

    public final TentativeTable table;

    public TableResponse(TentativeTable table) {
        this.table = table;
    }
}
