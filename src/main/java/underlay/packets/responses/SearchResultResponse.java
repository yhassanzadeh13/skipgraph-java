package underlay.packets.responses;

import node.skipgraph.SearchResult;
import underlay.packets.Response;

/**
 * Response for search result.
 */
public class SearchResultResponse extends Response {

  public final SearchResult result;

  public SearchResultResponse(SearchResult result) {
    this.result = result;
  }
}
