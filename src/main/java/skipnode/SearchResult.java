package skipnode;

import java.io.Serializable;

/**
 * Represents the result of a search containing the piggybacked information.
 */
public class SearchResult implements Serializable {

  // todo: this should be renamed to identity and also should be private.
  // todo: we should also record the search path.
  public final Identity result;

  public SearchResult(Identity result) {
    this.result = result;
  }
}
