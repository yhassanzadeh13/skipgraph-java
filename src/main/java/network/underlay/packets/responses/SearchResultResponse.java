package network.underlay.packets.responses;

import network.underlay.packets.Response;
import skipnode.SearchResult;

/**
 * Response for search result.
 */
public class SearchResultResponse extends Response {

  public final SearchResult result;

  public SearchResultResponse(SearchResult result) {
    this.result = result;
  }
}
